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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

/**
 * Fragment for editing the details of an existing event.
 * Allows the user to update event information such as name, location, description,
 * and maximum attendees. It also enables image editing for the event and provides
 * an option to delete the event.
 */
public class EditEventFragment extends Fragment {
    private Button saveButton, editImageButton, deleteButton;
    private FloatingActionButton backButton;
    private EditText eventNameForm, eventDescriptionForm, attendeesForm;
    private TextView pageHeader;
    private ImageView eventImageView;
    private Uri newImageUri, existingImageUri;
    protected ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    newImageUri = result.getData().getData();
                    eventImageView.setImageURI(newImageUri);
                }
            });
    private Long announcementCount;
    private ProgressBar progressBar;
    private PlacesClient placesClient;
    private EventController eventController;
    private AttendeeController attendeeController;
    private String eventID, eventLocation;
    private AutocompleteSupportFragment autocompleteFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_edit_event, container, false);
        ((MainActivity) requireActivity()).setNavbarVisibility(false);
        initializeUI(view);
        pageHeader.setText("Edit Event");

        attendeeController = new AttendeeController(FirebaseFirestore.getInstance());
        eventController = new EventController();
        eventID = requireArguments().getString("event_id");
        placesClient = Places.createClient(requireActivity());

        setupEventDetails(eventID);
        setListeners();

        return view;
    }

    /**
     * Initializes the UI components of the fragment.
     * This method finds and binds the UI components to their respective fields.
     *
     * @param view The root view of the fragment's layout from which to find the UI components.
     */
    private void initializeUI(View view) {
        pageHeader = view.findViewById(R.id.add_edit_event_Header);
        saveButton = view.findViewById(R.id.add_edit_save_button);
        backButton = view.findViewById(R.id.add_edit_backButton);
        editImageButton = view.findViewById(R.id.add_edit_event_imageButton);
        deleteButton = view.findViewById(R.id.add_edit_deleteButton);
        eventNameForm = view.findViewById(R.id.add_edit_event_Name);
        eventDescriptionForm = view.findViewById(R.id.add_edit_event_description);
        attendeesForm = view.findViewById(R.id.add_edit_event_Attendees);
        eventImageView = view.findViewById(R.id.add_edit_event_ImageView);
        progressBar = view.findViewById(R.id.progressBar);
        autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY);
    }

    /**
     * Sets up listeners for the various buttons in the fragment.
     * This includes listeners for saving changes, navigating back, editing the image,
     * and deleting the event.
     */
    private void setListeners() {
        editImageButton.setOnClickListener(v -> openGallery());
        saveButton.setOnClickListener(v -> saveEventChanges());
        backButton.setOnClickListener(v -> navigateBack(false));
        deleteButton.setOnClickListener(v -> deleteEvent());
        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    eventLocation = place.getName();
                }

                @Override
                public void onError(@NonNull Status status) {
                }
            });
        }
    }

    /**
     * Fetches the details of an event to be edited and displays them in the UI.
     * This method uses the eventController to fetch the event details by its ID
     * and populates the UI components with the fetched event data.
     *
     * @param eventID The unique identifier of the event to fetch and display.
     */
    private void setupEventDetails(String eventID) {
        eventController.getEventById(eventID, new EventFetchCallback() {
            @Override
            public void onSuccess(Event event) {
                eventNameForm.setText(event.getName());
                autocompleteFragment.setText(event.getLocation());
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
     * Saves the changes made to the event details.
     * This method collects the updated event information from the UI components,
     * constructs an updated event object, and calls the eventController to save the changes.
     */
    private void saveEventChanges() {
        String eventName = eventNameForm.getText().toString();
        String eventDescription = eventDescriptionForm.getText().toString();
        int maxAttendees = Integer.parseInt(attendeesForm.getText().toString());

        if (eventName.isEmpty() || eventLocation == null || eventDescription.isEmpty()) {
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

        eventController.editEvent(event, newImageUri, new EventUpdateCallback() {
            @Override
            public void onSuccess(boolean status) {
                progressBar.setVisibility(View.GONE);
                navigateBack(false);
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Deletes the event after confirming the action with the user.
     * Displays a confirmation dialog to the user, and if confirmed, proceeds to delete the event
     * using the eventController and attendeeController to clean up associated data.
     */
    private void deleteEvent() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    saveButton.setEnabled(false);
                    editImageButton.setEnabled(false);
                    deleteButton.setEnabled(false);

                    attendeeController.deleteAllAttendeesForEvent(eventID, new DeleteAllAttendeesCallback() {
                        @Override
                        public void onSuccess() {
                            eventController.deleteEvent(eventID, new EventDeleteCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(getContext(), "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    navigateBack(true);
                                }

                                @Override
                                public void onError(Exception e) {
                                    progressBar.setVisibility(View.GONE);
                                    saveButton.setEnabled(true);
                                    editImageButton.setEnabled(true);
                                    deleteButton.setEnabled(true);
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            progressBar.setVisibility(View.GONE);
                            saveButton.setEnabled(true);
                            editImageButton.setEnabled(true);
                            deleteButton.setEnabled(true);
                        }
                    });
                })
                .setNegativeButton(android.R.string.no, null).show();
    }


    /**
     * Opens the device's gallery for the user to pick an image.
     * The selected image is then set as the new image URI for the event.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    /**
     * Navigates back to the previous screen.
     * Ensures that the fragment is currently added to its activity before performing the navigation.
     */
    private void navigateBack(boolean delete) {
        if (isAdded()) {
            NavController navController = NavHostFragment.findNavController(this);
            if (delete) navController.navigate(R.id.eventsPage);
            else navController.popBackStack();
            ((MainActivity) requireActivity()).setNavbarVisibility(true);
        }
    }
}
