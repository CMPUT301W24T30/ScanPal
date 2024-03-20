package com.example.scanpal;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen.installSplashScreen(this); // show splash screen

        super.onCreate(savedInstanceState);

        setContentView(R.layout.nav_host);
        createNotificationChannel();
        setupNavController();

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /*CharSequence name = "Test Name Channel";//getString(R.string.channel_name);
            String description = "test description";// getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID_Scanpal", name, importance);
            channel.setDescription(description);
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            //NotificationManager notificationManager = getSystemService(NotificationManager.class);
            //notificationManager.createNotificationChannel(channel);
            */
        }

        // Create an explicit intent for an Activity in your app.

        Intent intent = new Intent(this,  MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        /*//Notifications stuff
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID_Scanpal")
                .setSmallIcon(R.drawable.peepo_user)
                .setContentTitle("Test Notification")
                .setContentText("This is a test notification test permissions")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(0,builder.build());*/
        //notificationManager.notify(1,builder.build());

    }

    // Sets up navigation
    private void setupNavController() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        UserController userController = new UserController(FirebaseFirestore.getInstance(), this);
        if (userController.isUserLoggedIn()) {
            navController.navigate(R.id.eventsPage);
        } else {
            navController.navigate(R.id.signupFragment);
        }
    }

}