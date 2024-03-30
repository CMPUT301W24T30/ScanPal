package com.example.scanpal.Fragments;

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
import com.example.scanpal.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
        FloatingActionButton backButton = view.findViewById(R.id.button_go_back);

        assert getArguments() != null;

        String eventID = getArguments().getString("event_id");
        String request = getArguments().getString("request");
        String eventName = getArguments().getString("eventName");

        backButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(ShowQrFragment.this);
            navController.popBackStack();
        });

        // Show either check in code or event code based on request
        if (request == "check-in") {
            showCheckIn(eventID,view,eventName);
        } else if (request == "event") {
            TextView textView = view.findViewById(R.id.show_qr_title);
            textView.setText("Event Details");
            showEvent(eventID,view,eventName);
        }

        return view;
    }

    public void showCheckIn(String eventID,View view,String eventName) {
        String imageName = eventID + "-check-in.png";
        StorageReference imageRef = storage.getReference().child("qr-codes/" + imageName);
        //View view = getView();
        assert view != null;
        ImageView imageView = view.findViewById(R.id.qr_code);

        // Get image from Firebase Storage
        imageRef.getBytes(1024 * 1024) // allow 1MB
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Load bytes into the ImageView using Glide
                        Glide.with(ShowQrFragment.this)
                                .load(bytes)
                                .into(imageView);
                        TextView title = view.findViewById(R.id.event_name_qrcode);
                        title.setText(eventName);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Toast.makeText(getContext(), "Failed to retrieve image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void showEvent(String eventID,View view,String eventName) {
        String imageName = eventID + "-event.png";
        StorageReference imageRef = storage.getReference().child("qr-codes/" + imageName);
        //View view = getView();
        assert view != null;
        ImageView imageView = view.findViewById(R.id.qr_code);

        // Get image from Firebase Storage
        imageRef.getBytes(1024 * 1024) // allow 1MB
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Load bytes into the ImageView using Glide
                        Glide.with(ShowQrFragment.this)
                                .load(bytes)
                                .into(imageView);
                        TextView title = view.findViewById(R.id.event_name_qrcode);
                        title.setText(eventName);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Toast.makeText(getContext(), "Failed to retrieve image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
