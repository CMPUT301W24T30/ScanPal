package com.example.scanpal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.scanpal.Callbacks.QrScanResultCallback;
import com.example.scanpal.Callbacks.UserFetchCallback;
import com.example.scanpal.Controllers.AttendeeController;
import com.example.scanpal.Controllers.QrScannerController;
import com.example.scanpal.Controllers.UserController;
import com.example.scanpal.Models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton buttonScan, buttonChat, buttonProfile, buttonYourEvents, buttonHomepage;
    private TextView textHome, textEvents, textChat, textProfile;
    private NavController navController;
    private View appBar;
    private ActivityResultLauncher<ScanOptions> qrCodeScanner;
    private QrScannerController qrScannerController;
    private int buttonChatColorFlag = -1; // Default color

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.d("msg", "broadcast received MAIN: ");
            updateUI(message);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen.installSplashScreen(this); // show splash screen

        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_host);
        setNavbarVisibility(false);
        initializeViews();
        setupButtonListeners();
        setupNavController();

        //receiver notif stuff
        IntentFilter filter = new IntentFilter("com.example.scanpal.MESSAGE_RECEIVED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("msg", "build good");
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        }


        // In case user has been deleted on firebase.
        new UserController(this).getUserFirebaseOnly(
                new UserController(this).fetchStoredUsername(),
                new UserFetchCallback() {
                    @Override
                    public void onSuccess(User user) {
                    }

                    @Override
                    public void onError(Exception e) {
                        navController.navigate(R.id.signupFragment);
                    }
                }
        );
    }

    // Set up navigation & navbar
    private void animateText(TextView showText, TextView... hideTexts) {
        showText.animate()
                .alpha(1f)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        for (TextView text : hideTexts) {
            text.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void setupNavController() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            buttonChat.setColorFilter(getResources().getColor(R.color.default_icon_tint));
            buttonProfile.setColorFilter(getResources().getColor(R.color.default_icon_tint));
            buttonYourEvents.setColorFilter(getResources().getColor(R.color.default_icon_tint));
            buttonHomepage.setColorFilter(getResources().getColor(R.color.default_icon_tint));

            if (buttonChatColorFlag == 0) {
                buttonChat.setColorFilter(getResources().getColor(R.color.button_alert));
            }

            if (destination.getId() == R.id.notificationsFragment) {
                buttonChatColorFlag = -1;
                buttonChat.setColorFilter(getResources().getColor(R.color.button_default));
                animateText(textChat, textHome, textEvents, textProfile);
            } else if (destination.getId() == R.id.profile_fragment) {
                buttonProfile.setColorFilter(getResources().getColor(R.color.button_default));
                animateText(textProfile, textHome, textEvents, textChat);
            } else if (destination.getId() == R.id.yourEvents) {
                buttonYourEvents.setColorFilter(getResources().getColor(R.color.button_default));
                animateText(textEvents, textHome, textChat, textProfile);
            } else if (destination.getId() == R.id.eventsPage) {
                buttonHomepage.setColorFilter(getResources().getColor(R.color.button_default));
                animateText(textHome, textEvents, textChat, textProfile);
            }
        });

        UserController userController = new UserController(this);
        if (userController.isUserLoggedIn()) {
            setNavbarVisibility(true);
            navController.navigate(R.id.eventsPage);
        } else {
            navController.navigate(R.id.signupFragment);
            setNavbarVisibility(false);
        }
    }

    /**
     * Initializes the view components of the fragment.
     */
    private void initializeViews() {
        buttonScan = findViewById(R.id.button_scan);
        buttonChat = findViewById(R.id.button_chat);
        buttonProfile = findViewById(R.id.button_profile);
        buttonYourEvents = findViewById(R.id.button_your_events);
        buttonHomepage = findViewById(R.id.button_homepage);
        appBar = findViewById(R.id.app_bar);
        textHome = findViewById(R.id.text_homepage);
        textEvents = findViewById(R.id.text_your_events);
        textChat = findViewById(R.id.text_chat);
        textProfile = findViewById(R.id.text_profile);
    }

    /**
     * Sets up listeners for the various buttons in the profile fragment.
     */
    private void setupButtonListeners() {
        AttendeeController attendeeController = new AttendeeController(FirebaseFirestore.getInstance());
        qrScannerController = new QrScannerController(this, attendeeController);

        // Initialize QR Code Scanner and set up scan button.
        qrCodeScanner = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                UserController userController = new UserController(this);
                String username = userController.fetchStoredUsername();
                qrScannerController.handleResult(result.getContents(), username, new QrScanResultCallback() {
                    @Override
                    public void onResult(String eventID) {
                        if (eventID != null) {
                            Bundle bundle = new Bundle();
                            bundle.putString("event_id", eventID);
                            navController.navigate(R.id.eventDetailsPage, bundle);  // pass id to event details fragment to display event scanned
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                    }
                });
            }
        });

        buttonScan.setOnClickListener(v -> {
            animateText(textHome, textEvents, textChat, textProfile);
            qrCodeScanner.launch(QrScannerController.getOptions());
        });
        buttonChat.setOnClickListener(v -> navController.navigate(R.id.notificationsFragment));
        buttonProfile.setOnClickListener(v -> navController.navigate(R.id.profile_fragment));
        buttonYourEvents.setOnClickListener(v -> navController.navigate(R.id.yourEvents));
        buttonHomepage.setOnClickListener(v -> navController.navigate(R.id.eventsPage));
    }

    public void setNavbarVisibility(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        if (buttonScan != null) buttonScan.setVisibility(visibility);
        if (buttonChat != null) buttonChat.setVisibility(visibility);
        if (buttonProfile != null) buttonProfile.setVisibility(visibility);
        if (buttonYourEvents != null) buttonYourEvents.setVisibility(visibility);
        if (buttonHomepage != null) buttonHomepage.setVisibility(visibility);
        if (appBar != null) appBar.setVisibility(visibility);
        if (textHome != null) textHome.setVisibility(visibility);
        if (textEvents != null) textEvents.setVisibility(visibility);
        if (textChat != null) textChat.setVisibility(visibility);
        if (textProfile != null) textProfile.setVisibility(visibility);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }


    /**
     * Method accordingly updates the UI based on Received cloud messages
     *
     * @param message the message that was sent
     */
    private void updateUI(String message) {
        if (buttonChat != null) {
            Log.d("msg", "MAKE RED: " + "message.getNotification().getBody())");
            buttonChatColorFlag = 0;
            buttonChat.setColorFilter(getResources().getColor(R.color.button_alert));
        }
    }
}
