package com.example.scanpal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ProfileFragment extends Fragment {

    private ImageView profileImageView;
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

        username = (TextInputEditText) ((TextInputLayout) view.findViewById(R.id.username)).getEditText();
        firstName = (TextInputEditText) ((TextInputLayout) view.findViewById(R.id.first_name)).getEditText();
        lastName = (TextInputEditText) ((TextInputLayout) view.findViewById(R.id.last_name)).getEditText();

        imageController = new ImageController();

        uploadButton.setOnClickListener(v -> openGallery());

        deleteButton.setOnClickListener(v -> profileImageView.setImageDrawable(null));

        fetchImageFromFirebase();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        imageController.uploadImage(imageUri,
                taskSnapshot -> Toast.makeText(getContext(), "Image Uploaded Successfully 🙏", Toast.LENGTH_SHORT)
                        .show(),
                e -> Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchImageFromFirebase() {
        // wip
        String imageUrl = "https://firebasestorage.googleapis.com/v0/b/scanpal-15383.appspot.com/o/onphone.png?alt=media&token=fcfebed3-8b32-464d-a77d-7a37e5dc2ad1";
        Glide.with(this)
                .load(imageUrl)
                .into(profileImageView);
    }
}
