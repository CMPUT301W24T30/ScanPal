package com.example.scanpal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

    private ImageView profileImageView;
    private TextInputEditText username, firstName, lastName;
    private ImageController imageController;
    private Uri imageUri;
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    profileImageView.setImageURI(imageUri);
                    uploadImageToFirebase(imageUri);
                }
            });
    private UserController userController;

    /**
     * Inflates the layout for the edit profile page.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.edit_profile_page, container, false);
    }

    /**
     * Initializes UI components, sets up button listeners, and fetches user details
     * after the view is created.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileImageView = view.findViewById(R.id.imageView);
        FloatingActionButton uploadButton = view.findViewById(R.id.upload_button);
        FloatingActionButton deleteButton = view.findViewById(R.id.delete_button);
        Button saveButton = view.findViewById(R.id.save_button);
        FloatingActionButton goBack = view.findViewById(R.id.button_go_back);

        username = (TextInputEditText) ((TextInputLayout) view.findViewById(R.id.username)).getEditText();
        if (username != null) {
            username.setEnabled(false);
        }
        firstName = (TextInputEditText) ((TextInputLayout) view.findViewById(R.id.first_name)).getEditText();
        lastName = (TextInputEditText) ((TextInputLayout) view.findViewById(R.id.last_name)).getEditText();

        imageController = new ImageController();
        userController = new UserController(FirebaseFirestore.getInstance(), getContext());

        uploadButton.setOnClickListener(v -> openGallery());
        deleteButton.setOnClickListener(v -> profileImageView.setImageDrawable(null));

        saveButton.setOnClickListener(v -> saveUserDetails());
        goBack.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EditProfileFragment.this);
            navController.navigate(R.id.save_profile_edits);
        });

        fetchUserDetails();
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
     * Uploads the selected image to Firebase Storage and updates the user's profile image URL.
     *
     * @param imageUri The URI of the image selected by the user.
     */
    private void uploadImageToFirebase(Uri imageUri) {
        imageController.uploadImage(imageUri,
                taskSnapshot -> Toast.makeText(getContext(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show(),
                e -> Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Fetches the current details of the user from Firestore/Internal storage and displays them in the UI components.
     */
    private void fetchUserDetails() {
        String storedUsername = userController.fetchStoredUsername();
        if (storedUsername != null) {
            userController.getUser(storedUsername, new UserFetchCallback() {
                @Override
                public void onSuccess(User user) {
                    username.setText(user.getUsername());
                    firstName.setText(user.getFirstName());
                    lastName.setText(user.getLastName());

                    if (user.getPhoto() != null) {
                        Glide.with(EditProfileFragment.this)
                                .load(user.getPhoto())
                                .apply(new RequestOptions().circleCrop())
                                .into(profileImageView);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getContext(), "Failed to fetch user details: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Saves the updated user details to Firestore and updates the local user serialization.
     */
    private void saveUserDetails() {
        User updatedUser = new User(Objects.requireNonNull(username.getText()).toString(), Objects.requireNonNull(firstName.getText()).toString(), Objects.requireNonNull(lastName.getText()).toString());
        if (imageUri != null) {
            updatedUser.setPhoto(imageUri.toString());
        }
        userController.updateUser(updatedUser, new UserUpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "User details updated successfully", Toast.LENGTH_SHORT).show();
                NavController navController = NavHostFragment.findNavController(EditProfileFragment.this);
                navController.navigate(R.id.save_profile_edits);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Failed to update user details: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
