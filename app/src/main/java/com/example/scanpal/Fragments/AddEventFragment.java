package com.example.scanpal.Fragments;

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

import com.example.scanpal.BuildConfig;
import com.example.scanpal.Callbacks.UserFetchCallback;
import com.example.scanpal.Controllers.EventController;
import com.example.scanpal.Controllers.ImageController;
import com.example.scanpal.Controllers.QrScannerController;
import com.example.scanpal.Controllers.UserController;
import com.example.scanpal.MainActivity;
import com.example.scanpal.Models.Event;
import com.example.scanpal.Models.User;
import com.example.scanpal.R;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Arrays;

/**
 * A Fragment for adding new events to the Firestore database. It allows users to input
 * details about a new event and save it. This class manages the UI for creating an event
 * and interacts with Firestore through {@link EventController} to store the event data.
 */
public class AddEventFragment extends Fragment {
    private static final String TAG = "AddEditEvent";
    Button saveButton;
    FloatingActionButton backButton;
    Button deleteButton;
    Button editImageButton;
    Button GenerateQrCodeButton;
    Button CustomQrCodeButton;
    Boolean QrChoice = Boolean.FALSE;
    String QrID = null;
    EditText attendeesForm;
    EditText eventNameForm;
    EditText eventDescriptionForm;
    Event newEvent;
    EventController eventController;
    UserController userController;
    ProgressBar progressBar;
    User Organizer;
    private Uri imageUri;
    private ImageView profileImageView;
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    profileImageView.setImageURI(imageUri);
                }
            });
    private ImageController imageController;
    private PlacesClient placesClient;
    private String selectedLocationName;
    private ActivityResultLauncher<ScanOptions> qrCodeScanner;


    public AddEventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // SDK init
        Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY);
        // Create a new Places client instance
        placesClient = Places.createClient(requireActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_edit_event, container, false);
        ((MainActivity) requireActivity()).setNavbarVisibility(false);

        TextView pageHeader = view.findViewById(R.id.add_edit_event_Header);
        pageHeader.setText("Create Event");

        this.deleteButton = view.findViewById(R.id.add_edit_deleteButton);
        this.deleteButton.setVisibility(View.GONE);


        this.saveButton = view.findViewById(R.id.add_edit_save_button);
        this.backButton = view.findViewById(R.id.add_edit_backButton);
        this.editImageButton = view.findViewById(R.id.add_edit_event_imageButton);
        this.attendeesForm = view.findViewById(R.id.add_edit_event_Attendees);
        this.eventNameForm = view.findViewById(R.id.add_edit_event_Name);
        this.eventDescriptionForm = view.findViewById(R.id.add_edit_event_description);
        this.profileImageView = view.findViewById(R.id.add_edit_event_ImageView);
        this.progressBar = view.findViewById(R.id.progressBar);

        imageController = new ImageController();
        userController = new UserController(FirebaseFirestore.getInstance(), view.getContext());
        eventController = new EventController();

        userController.getUser(userController.fetchStoredUsername(), new UserFetchCallback() {
            @Override
            public void onSuccess(User user) {
                Organizer = user;
            }

            @Override
            public void onError(Exception e) {
            }
        });

        // Initialize QR Code Scanner
        qrCodeScanner = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                // Ensure event doesn't already exist
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                DocumentSnapshot doc = database.collection("Events").document(result.getContents()).get().getResult();
                if (doc != null && doc.exists()) {
                    Toast.makeText(view.getContext(), "Event QR Code is in use", Toast.LENGTH_SHORT).show();
                } else {
                    if (result.getContents().startsWith("https://") || result.getContents().startsWith("www")) {  // ensure not a url
                        Toast.makeText(view.getContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(view.getContext(), "QR Code Scanned", Toast.LENGTH_SHORT).show();
                        QrID = result.getContents();
                        QrChoice = Boolean.TRUE;
                    }
                }
            } else {
                Toast.makeText(view.getContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show();
            }
        });

        this.newEvent = new Event(this.Organizer, "", "");

        saveButton.setOnClickListener(v -> {

            //checking for valid input
            if (eventNameForm.getText().toString().isEmpty() ||
                    selectedLocationName == null || selectedLocationName.isEmpty() ||
                    eventDescriptionForm.getText().toString().isEmpty() ||
                    attendeesForm.getText().toString().isEmpty() ||
                    null == imageUri||
                    QrChoice != Boolean.TRUE) {

                Toast.makeText(view.getContext(), "Please input all Information", Toast.LENGTH_LONG).show();

            } else if (Integer.parseInt(attendeesForm.getText().toString()) < 1) {
                Toast.makeText(view.getContext(), "Please allow at least 1 Attendee", Toast.LENGTH_LONG).show();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                newEvent.setName(eventNameForm.getText().toString());
                newEvent.setLocation(selectedLocationName);
                newEvent.setDescription(eventDescriptionForm.getText().toString());
                newEvent.setMaximumAttendees(Integer.parseInt(attendeesForm.getText().toString()));
                newEvent.setPosterURI(imageUri);
                newEvent.setAnnouncementCount(0L);

                eventController.addEvent(newEvent, QrID);
                uploadImageToFirebase(imageUri);

                progressBar.setVisibility(View.GONE);
                NavController navController = NavHostFragment.findNavController(AddEventFragment.this);
                navController.navigate(R.id.addEditEventComplete);
                ((MainActivity) requireActivity()).setNavbarVisibility(true);
            }
        });

        backButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(AddEventFragment.this);
            navController.navigate(R.id.addEditEventComplete);
            ((MainActivity) requireActivity()).setNavbarVisibility(true);
        });

        editImageButton.setOnClickListener(v -> openGallery());

        // Qr code buttons
        this.GenerateQrCodeButton = view.findViewById(R.id.generate_qr_code);
        GenerateQrCodeButton.setOnClickListener(v -> {
            QrChoice = Boolean.TRUE;
            QrID = null;
            Toast.makeText(view.getContext(), "Generated QR Code", Toast.LENGTH_SHORT).show();
            this.GenerateQrCodeButton.setVisibility(View.GONE);
            this.CustomQrCodeButton.setVisibility(View.GONE);
        });

        this.CustomQrCodeButton = view.findViewById(R.id.custom_qr_code);
        CustomQrCodeButton.setOnClickListener(v -> {
            // Request to scan qr code
            qrCodeScanner.launch(QrScannerController.getOptions());
            if (QrChoice) {
                this.GenerateQrCodeButton.setVisibility(View.GONE);
                this.CustomQrCodeButton.setVisibility(View.GONE);
            }
        });

        // Initialize Places and AutocompleteSupportFragment
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY);
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
     * Uploads the selected image to Firebase Storage, specifying a folder and filename.
     *
     * @param imageUri The URI of the image selected by the user.
     */
    private void uploadImageToFirebase(Uri imageUri) {
        String folderPath = "events";
        String fileName = "event_" + newEvent.getId() + ".jpg";

        imageController.uploadImage(imageUri, folderPath, fileName,
                uri -> System.out.println("Success"),
                e -> Log.e("AddEventFragment", "Upload failed: " + e.getMessage()));
    }
}
