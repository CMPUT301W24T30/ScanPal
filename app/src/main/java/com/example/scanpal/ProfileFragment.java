package com.example.scanpal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment for displaying and managing user profile information. This fragment allows users
 * to view their profile details including their username, first name, last name, and profile image.
 * Users can navigate to other parts of the application such as editing profile,
 * and viewing events from this fragment.
 */
public class ProfileFragment extends Fragment {

    private ImageView profileImageView;
    private FloatingActionButton buttonScan, buttonGoBack, buttonChat, buttonProfile, buttonYourEvents, buttonHomepage,
            buttonEditProfile;
    private TextView addUsername, firstName, lastName, homepage;
    private UserController userController;

    /**
     * Inflates the layout for the user's profile page.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_profile_page, container, false);
    }

    /**
     * Initializes the UI components, sets up button listeners, and fetches user details
     * after the view is created.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupButtonListeners();

        userController = new UserController(FirebaseFirestore.getInstance(), getContext());

        String username = userController.fetchStoredUsername();
        if (username != null) {
            fetchUserDetails(username);
        } else {
            Toast.makeText(getContext(), "No username found in internal storage", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Refreshes the user's profile details when the fragment is resumed (i.e. when navigated back to).
     */
    @Override
    public void onResume() {
        super.onResume();
        String username = userController.fetchStoredUsername();
        if (username != null) {
            fetchUserDetails(username);
        } else {
            Toast.makeText(getContext(), "No username found in internal storage", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Initializes the view components of the fragment.
     *
     * @param view The view returned by onCreateView.
     */
    private void initializeViews(View view) {
        profileImageView = view.findViewById(R.id.imageView);
        buttonScan = view.findViewById(R.id.button_scan);
        buttonGoBack = view.findViewById(R.id.button_go_back);
        buttonChat = view.findViewById(R.id.button_chat);
        buttonProfile = view.findViewById(R.id.button_profile);
        buttonYourEvents = view.findViewById(R.id.button_your_events);
        buttonHomepage = view.findViewById(R.id.button_homepage);
        buttonEditProfile = view.findViewById(R.id.button_edit_profile);
        addUsername = view.findViewById(R.id.addUsername);
        firstName = view.findViewById(R.id.first_name);
        lastName = view.findViewById(R.id.last_name);
        homepage = view.findViewById(R.id.homepage);
    }

    /**
     * Sets up listeners for the various buttons in the profile fragment.
     */
    private void setupButtonListeners() {
        buttonScan.setOnClickListener(v -> {
            // TODO
        });

        buttonGoBack.setOnClickListener(v -> {
            // TODO
        });

        buttonChat.setOnClickListener(v -> {
            // TODO
        });

        buttonProfile.setOnClickListener(v -> {
            // TODO
        });

        buttonYourEvents.setOnClickListener(v -> {
            // TODO
        });

        buttonHomepage.setOnClickListener(v -> {
            // TODO
        });

        buttonEditProfile.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(ProfileFragment.this);
            navController.navigate(R.id.edit_profile);
        });
    }

    /**
     * Fetches and displays the details of the user from Firebase/Internal storage.
     *
     * @param username The username of the user whose details are to be fetched.
     */
    private void fetchUserDetails(String username) {
        userController.getUser(username, new UserFetchCallback() {
            @Override
            public void onSuccess(User user) {
                requireActivity().runOnUiThread(() -> {
                    addUsername.setText(user.getUsername());
                    firstName.setText(user.getFirstName());
                    lastName.setText(user.getLastName());
                    Glide.with(ProfileFragment.this)
                            .load(user.getPhoto())
                            .into(profileImageView);
                });
            }

            @Override
            public void onError(Exception e) {
            }
        });
    }
}
