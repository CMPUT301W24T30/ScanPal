package com.example.scanpal.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.scanpal.Callbacks.DeleteAllAttendeesCallback;
import com.example.scanpal.Callbacks.EventDeleteCallback;
import com.example.scanpal.Callbacks.UserFetchCallback;
import com.example.scanpal.Callbacks.UserRemoveCallback;
import com.example.scanpal.Callbacks.UserUpdateCallback;
import com.example.scanpal.Controllers.AttendeeController;
import com.example.scanpal.Controllers.EventController;
import com.example.scanpal.Controllers.ImageController;
import com.example.scanpal.Controllers.UserController;
import com.example.scanpal.MainActivity;
import com.example.scanpal.Models.User;
import com.example.scanpal.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

/**
 * Fragment for editing and updating user profile information.
 * Users can change their first name, last name, and profile image.
 * Username cannot be edited.
 * Changes are updated in Firestore and locally.
 */
public class EditProfileFragment extends Fragment {

    protected User existingUser;
    private ImageView profileImageView;
    private ProgressBar progressBar;
    private TextInputEditText username, firstName, lastName, homepage;
    private boolean isDeleteIntent = false;
    private ImageController imageController;
    private Uri imageUri;
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    Glide.with(this)
                            .load(imageUri)
                            .circleCrop()
                            .into(profileImageView);
                }
            });
    private UserController userController;
    private AttendeeController attendeeController;
    private EventController eventController;

    /**
     * Inflates the layout for the edit profile page.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).setNavbarVisibility(false);
        return inflater.inflate(R.layout.edit_profile_page, container, false);
    }

    /**
     * Initializes UI components, sets up button listeners, and fetches user details
     * after the view is created.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileImageView = view.findViewById(R.id.profile_page_image);
        FloatingActionButton uploadButton = view.findViewById(R.id.upload_button);
        FloatingActionButton deleteButton = view.findViewById(R.id.delete_button);
        Button saveButton = view.findViewById(R.id.save_button);
        FloatingActionButton goBack = view.findViewById(R.id.button_go_back);
        FloatingActionButton resetButton = view.findViewById(R.id.reset_button);
        progressBar = view.findViewById(R.id.progressBar);

        username = (TextInputEditText) ((TextInputLayout) view.findViewById(R.id.username)).getEditText();
        if (username != null) {
            username.setEnabled(false);
        }
        firstName = (TextInputEditText) ((TextInputLayout) view.findViewById(R.id.first_name)).getEditText();
        lastName = (TextInputEditText) ((TextInputLayout) view.findViewById(R.id.last_name)).getEditText();
        homepage = (TextInputEditText) ((TextInputLayout) view.findViewById(R.id.homepage)).getEditText();

        imageController = new ImageController();
        userController = new UserController(getContext());
        attendeeController = new AttendeeController(FirebaseFirestore.getInstance());
        eventController = new EventController();

        uploadButton.setOnClickListener(v -> openGallery());

        deleteButton.setOnClickListener(v -> {
            imageUri = null;
            isDeleteIntent = true;
            Glide.with(EditProfileFragment.this)
                    .load(R.drawable.ic_launcher_background)
                    .apply(new RequestOptions().circleCrop())
                    .into(profileImageView);
        });

        fetchUserDetails();

        saveButton.setOnClickListener(v -> saveUserDetails());
        goBack.setOnClickListener(v -> {
            // This is to make it send back to the right profile in admin
            Bundle bundle = new Bundle();
            bundle.putString("username", existingUser.getUsername());
            NavController navController = NavHostFragment.findNavController(EditProfileFragment.this);
            navController.navigate(R.id.save_profile_edits, bundle);
            ((MainActivity) requireActivity()).setNavbarVisibility(true);
        });
        resetButton.setOnClickListener(v -> showDeleteConfirmation());

    }

    /**
     * Opens the device's gallery for the user to pick a new profile image.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    /**
     * Fetches the current details of the user from Firestore/Internal storage and displays them in the UI components.
     */
    private void fetchUserDetails() {
        String username = "";
        if (getArguments() != null) {
            username = getArguments().getString("username", "");
        }
        if (Objects.equals(username, "")) {
            username = userController.fetchStoredUsername();
        }

        if (username != null) {
            userController.getUser(username, new UserFetchCallback() {
                @Override
                public void onSuccess(User user) {
                    existingUser = user;
                    EditProfileFragment.this.username.setText(user.getUsername());
                    firstName.setText(user.getFirstName());
                    lastName.setText(user.getLastName());
                    homepage.setText(user.getHomepage());

                    if (user.getPhoto() != null) {
                        Glide.with(EditProfileFragment.this)
                                .load(user.getPhoto())
                                .apply(new RequestOptions().circleCrop())
                                .into(profileImageView);
                    }
                }

                @Override
                public void onError(Exception e) {
                }
            });
        }
    }

    /**
     * Saves the updated user details to Firestore and updates the local user serialization.
     * If a new image was selected, uploads it to Firebase Storage and updates the user's profile image URL.
     * Otherwise, uses the existing photo URL.
     */
    private void saveUserDetails() {
        progressBar.setVisibility(View.VISIBLE);

        userController.getUser(existingUser.getUsername(), new UserFetchCallback() {
            @Override
            public void onSuccess(User existingUser) {

                User updatedUser = new User(Objects.requireNonNull(username.getText()).toString(),
                        Objects.requireNonNull(firstName.getText()).toString(),
                        Objects.requireNonNull(lastName.getText()).toString(),
                        null,
                        Objects.requireNonNull(homepage.getText()).toString(),
                        existingUser.getDeviceToken());

                if (existingUser.isAdministrator()) {
                    updatedUser.setAdministrator(true);
                }

                if (imageUri != null) {
                    String folderPath = "profile_images";
                    String fileName = existingUser.getUsername() + ".jpg";

                    imageController.uploadImage(imageUri, folderPath, fileName, uri -> {
                        updatedUser.setPhoto(uri.toString());
                        updateUserInFirestore(updatedUser);
                    }, e -> progressBar.setVisibility(View.GONE));
                } else if (isDeleteIntent) {
                    String defaultImageUrl = existingUser.createProfileImage(existingUser.getUsername());
                    updatedUser.setPhoto(defaultImageUrl);
                    updateUserInFirestore(updatedUser);
                } else {
                    updatedUser.setPhoto(existingUser.getPhoto());
                    updateUserInFirestore(updatedUser);
                }
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }


    /**
     * Updates the user details in Firestore and handles success or failure.
     *
     * @param user The user object containing updated details.
     */
    private void updateUserInFirestore(User user) {
        userController.updateUser(user, new UserUpdateCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "User details updated successfully 🎉", Toast.LENGTH_SHORT).show();
                NavController navController = NavHostFragment.findNavController(EditProfileFragment.this);
                navController.navigate(R.id.save_profile_edits);
                ((MainActivity) requireActivity()).setNavbarVisibility(true);
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                ((MainActivity) requireActivity()).setNavbarVisibility(true);
            }
        });
    }

    /**
     * Shows a confirmation dialog to confirm user deletion.
     */
    private void showDeleteConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Profile?")
                .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser())
                .setNegativeButton("Cancel", null)
                .setIcon(R.drawable.danger_icon)
                .show();
    }

    /**
     * Handles the user and linked attendees deletion process.
     *
     */
    private void deleteUser() {
        String username = existingUser.getUsername();
        if (username != null) {
            String oldUsername = userController.fetchStoredUsername();
            attendeeController.deleteAllUserAttendees(username, new DeleteAllAttendeesCallback() {
                @Override
                public void onSuccess() {
                    userController.removeUser(username, new UserRemoveCallback() {
                        @Override
                        public void onSuccess() {
                            eventController.deleteEventsByOrganizer(username, new EventDeleteCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(getContext(), "Profile reset successfully", Toast.LENGTH_SHORT).show();
                                    NavController navController = NavHostFragment.findNavController(EditProfileFragment.this);
                                    if (Objects.equals(oldUsername, username)) {
                                        navController.navigate(R.id.signupFragment);
                                    } else {
                                        navController.navigate(R.id.eventsPage);
                                        ((MainActivity) requireActivity()).setNavbarVisibility(true);
                                    }
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                }
            });
        }
    }
}