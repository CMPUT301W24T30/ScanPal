package com.example.scanpal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class ShowQrFragment extends Fragment {

    public ShowQrFragment() {
        // Required empty public constructor
    }

    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.show_qr, container, false);

        //assert getArguments() != null;
        // String eventID = getArguments().getString("event_id");
        String imageName = "c121e9c7-800f-4942-8b65-6a56a88106f8-check-in.png";
        StorageReference imageRef = storage.getReference().child("qr-codes/" + imageName);
        ImageView imageView = view.findViewById(R.id.qr_code);

        // Retrieve the image from Firebase Storage
        imageRef.getBytes(1024 * 1024)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Load the retrieved image bytes into the ImageView using Glide
                        Glide.with(ShowQrFragment.this)
                                .load(bytes)
                                .into(imageView);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure to retrieve image
                        Toast.makeText(getContext(), "Failed to retrieve image", Toast.LENGTH_SHORT).show();
                    }
                });

        return view;
    }

}
