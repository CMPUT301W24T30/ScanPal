package com.example.scanpal;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
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
        setupNavController();

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