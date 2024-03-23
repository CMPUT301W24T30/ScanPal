package com.example.scanpal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.api.Status;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

/**
 * A Fragment for adding new events to the Firestore database. It allows users to input
 * details about a new event and save it. This class manages the UI for creating an event
 * and interacts with Firestore through {@link EventController} to store the event data.
 */
public class AddEventFragment extends Fragment {
    Button saveButton;
    FloatingActionButton backButton;
    Button deleteButton;
    Button editImageButton;
    EditText attendeesForm;
    EditText eventNameForm;
    EditText eventDescriptionForm;
    Event newEvent;
    EventController eventController;
    UserController userController;
    User Organizer;
    private Uri imageUri;
    private ImageView profileImageView;
    private ImageController imageController;
    private PlacesClient placesClient;
    private String selectedLocationName;
    private static final String TAG = "AddEditEvent";


    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    profileImageView.setImageURI(imageUri);
                    uploadImageToFirebase(imageUri);
                }
            });


    public AddEventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // SDK init
        Places.initialize(requireContext(), getString(R.string.google_maps_key));
        // Create a new Places client instance
        placesClient = Places.createClient(requireActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_edit_event, null, false);

        //Fragment UI
        TextView pageHeader = view.findViewById(R.id.add_edit_event_Header);
        pageHeader.setText("Create Event");

        //TODO: Remove the delete button from page when creating event
        this.deleteButton = view.findViewById(R.id.add_edit_deleteButton);
        this.deleteButton.setVisibility(View.GONE); // no need for delete button when creating an event


        this.saveButton = view.findViewById(R.id.add_edit_save_button);
        this.backButton = view.findViewById(R.id.add_edit_backButton);
        this.editImageButton = view.findViewById(R.id.add_edit_event_imageButton);
        this.attendeesForm = view.findViewById(R.id.add_edit_event_Attendees);
        this.eventNameForm = view.findViewById(R.id.add_edit_event_Name);
        this.eventDescriptionForm = view.findViewById(R.id.add_edit_event_description);
        this.profileImageView = view.findViewById(R.id.add_edit_event_ImageView);

        imageController = new ImageController();


        //getting an instance of the currentUser
        userController = new UserController(FirebaseFirestore.getInstance(), view.getContext());
        eventController = new EventController();

        userController.getUser(userController.fetchStoredUsername(), new UserFetchCallback() {
            @Override
            public void onSuccess(User user) {
                Organizer = user;
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(view.getContext(), "Failed to fetch User Data", Toast.LENGTH_LONG).show();

                //TODO: probably should navigate back to the events page on failure?

            }
        });

        this.newEvent = new Event(this.Organizer, "", "");//blank event for now until user clicks 'save'


        //log test getting name
        Log.d("STORAGE", userController.fetchStoredUsername());

        //Implementing the Save button
        saveButton.setOnClickListener(v -> {

            //checking for valid input
            if (eventNameForm.getText().toString().isEmpty() ||
                    selectedLocationName == null || selectedLocationName.isEmpty() ||
                    eventDescriptionForm.getText().toString().isEmpty() ||
                    attendeesForm.getText().toString().isEmpty() ||
                    null == imageUri) {

                Toast.makeText(view.getContext(), "Please input all Information", Toast.LENGTH_LONG).show();

            } else if (Integer.parseInt(attendeesForm.getText().toString()) < 1) {
                Toast.makeText(view.getContext(), "Please allow at least 1 Attendee", Toast.LENGTH_LONG).show();
            } else {
                newEvent.setName(eventNameForm.getText().toString());
                newEvent.setLocation(selectedLocationName);
                newEvent.setDescription(eventDescriptionForm.getText().toString());
                newEvent.setMaximumAttendees(Integer.parseInt(attendeesForm.getText().toString()));
                newEvent.setPosterURI(imageUri);
                newEvent.setAnnouncementCount(0L);

                //now add the new event to the database
                eventController.addEvent(newEvent);

                NavController navController = NavHostFragment.findNavController(AddEventFragment.this);
                navController.navigate(R.id.addEditEventComplete);
            }

        });


        //Implementing the Back button to return to Events Page
        backButton.setOnClickListener(v -> {

            //just go back to the previous screen without doing anything
            NavController navController = NavHostFragment.findNavController(AddEventFragment.this);
            navController.navigate(R.id.addEditEventComplete);
        });

        editImageButton.setOnClickListener(v -> {
            // what to do when trying to edit image
            openGallery();
        });

        // Initialize Places and AutocompleteSupportFragment
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key));
        }
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    // Handle the selected place
                    selectedLocationName = place.getName();
                }

                @Override
                public void onError(@NonNull Status status) {
                    Log.i(TAG, "An error occurred: " + status);
                }
            });
        }

        return view;
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

}
