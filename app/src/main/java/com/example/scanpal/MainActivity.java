package com.example.scanpal;

import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
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
    private NavController navController;
    private View appBar;
    private ActivityResultLauncher<ScanOptions> qrCodeScanner;
    private QrScannerController qrScannerController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen.installSplashScreen(this); // show splash screen

        super.onCreate(savedInstanceState);

        setContentView(R.layout.nav_host);
        setNavbarVisibility(false);
        initializeViews();
        setupButtonListeners();
        setupNavController();

        // In case user has been deleted on firebase.
        new UserController(this).getUserFirebaseOnly(
                new UserController(this).fetchStoredUsername(),
                new UserFetchCallback() {
                    @Override
                    public void onSuccess(User user) {
                        // Continue
                    }

                    @Override
                    public void onError(Exception e) {
                        navController.navigate(R.id.signupFragment);
                    }
                }
        );

    }

    // Sets up navigation
    private void setupNavController() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

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
            qrCodeScanner.launch(QrScannerController.getOptions());
        });

        buttonChat.setOnClickListener(v -> {
            navController.navigate(R.id.notificationsFragment);
        });

        buttonProfile.setOnClickListener(v -> {
            navController.navigate(R.id.profile_fragment);
        });

        buttonYourEvents.setOnClickListener(v -> {
            navController.navigate(R.id.yourEvents);
        });

        buttonHomepage.setOnClickListener(v -> {
            navController.navigate(R.id.eventsPage);
        });
    }

    public void setNavbarVisibility(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        if (buttonScan != null) buttonScan.setVisibility(visibility);
        if (buttonChat != null) buttonChat.setVisibility(visibility);
        if (buttonProfile != null) buttonProfile.setVisibility(visibility);
        if (buttonYourEvents != null) buttonYourEvents.setVisibility(visibility);
        if (buttonHomepage != null) buttonHomepage.setVisibility(visibility);
        if (appBar != null) appBar.setVisibility(visibility);
    }
}