package com.example.scanpal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NotificationsFragment extends Fragment {

    public NotificationsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notifications_screen, null, false);

        // Set up button to navigate to user profile.
        FloatingActionButton profileButton = view.findViewById(R.id.button_profile);
        profileButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(NotificationsFragment.this);
            navController.navigate(R.id.notifications_to_profile_fragment);
        });

        // Set up button to navigate to Homepage
        FloatingActionButton homeButton = view.findViewById(R.id.button_homepage);
        homeButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(NotificationsFragment.this);
            navController.navigate(R.id.notifications_to_eventsPage);
        });


        return view;
    }
}
