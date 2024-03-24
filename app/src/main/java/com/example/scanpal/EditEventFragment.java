package com.example.scanpal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Fragment for editing an existing event.
 */
public class EditEventFragment extends Fragment {

    private Button saveButton, editImageButton;
    Button GenerateQrCodeButton;
    Button CustomQrCodeButton;
    private FloatingActionButton backButton;
    private EditText eventNameForm, eventLocationForm, eventDescriptionForm, attendeesForm;
    private ImageView eventImageView;
    private Uri newImageUri;
    protected ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    newImageUri = result.getData().getData();
                    eventImageView.setImageURI(newImageUri);
                }
            });
    private Uri existingImageUri;
    private Long announcementCount;
    private ProgressBar progressBar;
    private EventController eventController;
    private String eventID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_edit_event, container, false);
        ((MainActivity) requireActivity()).setNavbarVisibility(false);

        initializeUI(view);
        eventController = new EventController();
        eventID = requireArguments().getString("event_id");
        setupEventDetails(eventID);

        editImageButton.setOnClickListener(v -> openGallery());
        saveButton.setOnClickListener(v -> saveEventChanges());
        backButton.setOnClickListener(v -> navigateBack());

        this.GenerateQrCodeButton = view.findViewById(R.id.generate_qr_code);
        this.CustomQrCodeButton = view.findViewById(R.id.custom_qr_code);
        this.GenerateQrCodeButton.setVisibility(View.GONE);
        this.CustomQrCodeButton.setVisibility(View.GONE);

        return view;
    }

    /**
     * Initializes the user interface components from the layout.
     * This method finds each UI component by its ID and binds it to a corresponding variable.
     *
     * @param view The root view of the fragment's layout.
     */
    private void initializeUI(View view) {
        saveButton = view.findViewById(R.id.add_edit_save_button);
        backButton = view.findViewById(R.id.add_edit_backButton);
        editImageButton = view.findViewById(R.id.add_edit_event_imageButton);
        eventNameForm = view.findViewById(R.id.add_edit_event_Name);
        eventLocationForm = view.findViewById(R.id.add_edit_event_Location);
        eventDescriptionForm = view.findViewById(R.id.add_edit_event_description);
        attendeesForm = view.findViewById(R.id.add_edit_event_Attendees);
        eventImageView = view.findViewById(R.id.add_edit_event_ImageView);
        progressBar = view.findViewById(R.id.progressBar);
    }

    /**
     * Fetches and displays the details of the event to be edited.
     * It calls the eventController to fetch event details by ID and populates the UI with the fetched data.
     *
     * @param eventID The ID of the event to fetch and display.
     */
    private void setupEventDetails(String eventID) {
        eventController.getEventById(eventID, new EventFetchCallback() {
            @Override
            public void onSuccess(Event event) {
                eventNameForm.setText(event.getName());
                eventLocationForm.setText(event.getLocation());
                eventDescriptionForm.setText(event.getDescription());
                attendeesForm.setText(String.valueOf(event.getMaximumAttendees()));
                existingImageUri = event.getPosterURI();
                announcementCount = event.getAnnouncementCount();

                Glide.with(EditEventFragment.this)
                        .load(event.getPosterURI())
                        .into(eventImageView);
            }

            @Override
            public void onError(Exception e) {
            }
        });
    }

    /**
     * Saves the changes made to the event.
     * Validates the input fields, constructs an Event object from the input data, and
     * calls the eventController to update the event. Displays a loading indicator while saving.
     */
    private void saveEventChanges() {
        String eventName = eventNameForm.getText().toString();
        String eventLocation = eventLocationForm.getText().toString();
        String eventDescription = eventDescriptionForm.getText().toString();
        int maxAttendees = Integer.parseInt(attendeesForm.getText().toString());

        if (eventName.isEmpty() || eventLocation.isEmpty() || eventDescription.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);
        editImageButton.setEnabled(false);

        Event event = new Event(null, eventName, eventDescription);
        event.setLocation(eventLocation);
        event.setMaximumAttendees(maxAttendees);
        event.setId(eventID);
        event.setPosterURI(existingImageUri);
        event.setAnnouncementCount(announcementCount);

//        eventController.editEvent(event, newImageUri, new EventUpdateCallback() {
//            @Override
//            public void onSuccess(boolean status) {
//                progressBar.setVisibility(View.GONE);
//                navigateBack();
//            }
//
//            @Override
//            public void onError(Exception e) {
//                progressBar.setVisibility(View.GONE);
//            }
//        });
    }

    /**
     * Opens the gallery for the user to pick an image.
     * The selected image's URI is set as the new image URI for the event.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    /**
     * Navigates back to the previous screen.
     * This method checks if the fragment is currently added to its activity before navigating back.
     */
    private void navigateBack() {
        if (isAdded()) {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
            ((MainActivity) requireActivity()).setNavbarVisibility(true);
        }
    }
}
