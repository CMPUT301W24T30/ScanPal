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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class EditProfileFragment extends Fragment {

    private ImageView profileImageView;
    private Button saveButton;
    private FloatingActionButton uploadButton, deleteButton;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.edit_profile_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileImageView = view.findViewById(R.id.imageView);
        uploadButton = view.findViewById(R.id.upload_button);
        deleteButton = view.findViewById(R.id.delete_button);
        saveButton = view.findViewById(R.id.save_button);

        username = (TextInputEditText) ((TextInputLayout) view.findViewById(R.id.username)).getEditText();
        firstName = (TextInputEditText) ((TextInputLayout) view.findViewById(R.id.first_name)).getEditText();
        lastName = (TextInputEditText) ((TextInputLayout) view.findViewById(R.id.last_name)).getEditText();

        imageController = new ImageController();
        userController = new UserController(FirebaseFirestore.getInstance(), getContext());

        uploadButton.setOnClickListener(v -> openGallery());
        deleteButton.setOnClickListener(v -> profileImageView.setImageDrawable(null));

        saveButton.setOnClickListener(v -> saveUserDetails());

        fetchUserDetails();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        imageController.uploadImage(imageUri,
                taskSnapshot -> Toast.makeText(getContext(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show(),
                e -> Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

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
                        Glide.with(EditProfileFragment.this).load(user.getPhoto()).into(profileImageView);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getContext(), "Failed to fetch user details: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void saveUserDetails() {
        User updatedUser = new User(Objects.requireNonNull(username.getText()).toString(), Objects.requireNonNull(firstName.getText()).toString(), Objects.requireNonNull(lastName.getText()).toString());
        if (imageUri != null) {
            updatedUser.setPhoto(imageUri.toString());
        }
        userController.addUser(updatedUser, new UserAddCallback() {
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
