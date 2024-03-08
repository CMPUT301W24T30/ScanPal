package com.example.scanpal;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventDetailsFragment extends Fragment {

    private String eventName;
    private FloatingActionButton eventEditButton;
    FloatingActionButton backButton;
    private String eventDescription;
    private String eventOrganizer;
    private String getEventOrganizerUserName;
    private String eventLocation;
    private User userDetails; // current user
    private ImageView eventPoster;
    private String ImageURI;

    public EventDetailsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_details, null, false);

        backButton = view.findViewById(R.id.event_details_backButton);
        eventPoster = view.findViewById(R.id.event_detail_imageView);

        backButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);
            navController.navigate(R.id.eventsPage);
        });

        if (getArguments() != null) {
            String eventID = getArguments().getString("eventId");
            if (eventID != null) {
                fetchEventDetails(eventID);
            } else {
                Log.e("EventDetailsFragment", "Event ID is null");
            }
        }

        // Setup the event edit button and other UI elements here

        return view;
    }

    private void fetchEventDetails(String EventID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventDocument = db.collection("Events").document(EventID);

        eventDocument.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    eventName = document.getString("name");
                    eventDescription = document.getString("description");
                    eventLocation = document.getString("location");
                    ImageURI = document.getString("photo");
                    DocumentReference organizerRef = document.getDocumentReference("organizer");

                    fetchOrganizer(organizerRef);

                    updateUI();
                } else {
                    Toast.makeText(getContext(), "Event doesn't exist", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e("EventDetailsFragment", "Failed to fetch event details", task.getException());
            }
        });
    }

    private void fetchOrganizer(DocumentReference organizerRef) {
        organizerRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String firstName = document.getString("firstName");
                    String lastName = document.getString("lastName");
                    getEventOrganizerUserName = document.getId();
                    eventOrganizer = firstName + " " + lastName;
                    updateOrganizerUI();
                } else {
                    Log.e("EventDetailsFragment", "Organizer document does not exist");
                }
            } else {
                Log.e("EventDetailsFragment", "Failed to get organizer document", task.getException());
            }
        });
    }

    private void updateUI() {
        if (getActivity() == null) return;

        TextView eventTitle = getActivity().findViewById(R.id.event_Title);
        TextView eventDes = getActivity().findViewById(R.id.event_description);
        TextView eventLoc = getActivity().findViewById(R.id.event_Location);

        getActivity().runOnUiThread(() -> {
            if (eventTitle != null) eventTitle.setText(eventName);
            if (eventDes != null) eventDes.setText(eventDescription);
            if (eventLoc != null) eventLoc.setText(eventLocation);
            if (eventPoster != null) Glide.with(EventDetailsFragment.this).load(ImageURI).into(eventPoster);
        });
    }

    private void updateOrganizerUI() {
        if (getActivity() == null) return;

        TextView OrganizerName = getActivity().findViewById(R.id.event_orgName);

        getActivity().runOnUiThread(() -> {
            if (OrganizerName != null) OrganizerName.setText(eventOrganizer);
        });
    }

    // Add other methods here if necessary
}
