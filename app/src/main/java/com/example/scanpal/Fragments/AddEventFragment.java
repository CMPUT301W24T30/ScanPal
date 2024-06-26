package com.example.scanpal.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
import com.example.scanpal.Callbacks.EventFetchByUserCallback;
import com.example.scanpal.Callbacks.EventIDsFetchCallback;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.firebase.messaging.FirebaseMessaging;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * A Fragment for adding new events to the Firestore database. It allows users to input
 * details about a new event and save it. This class manages the UI for creating an event
 * and interacts with Firestore through {@link EventController} to store the event data.
 */
public class AddEventFragment extends Fragment {
    private static final String TAG = "AddEditEvent";
    Button editImageButton;
    FloatingActionButton backButton, saveButton;
    Boolean QrChoice = Boolean.FALSE;
    String QrID = null;
    EditText eventNameForm, eventDescriptionForm;
    Event newEvent;
    ProgressBar progressBar;
    User Organizer;
    EventController eventController;
    UserController userController;
    private ImageController imageController;
    private ActivityResultLauncher<ScanOptions> qrCodeScanner;
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
    private PlacesClient placesClient;
    private String selectedLocationName, locationCoords, date, time;
    private TextInputEditText dateEditText, timeEditText, attendeesForm;

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

        MaterialSwitch trackLocationSwitch = view.findViewById(R.id.track_location_switch);

        trackLocationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (newEvent != null) {
                newEvent.setTrackLocation(isChecked);
            }
        });

        TextView pageHeader = view.findViewById(R.id.add_edit_event_Header);
        pageHeader.setText("Create ✏️");

        this.saveButton = view.findViewById(R.id.add_edit_save_button);
        this.backButton = view.findViewById(R.id.add_edit_backButton);
        this.editImageButton = view.findViewById(R.id.add_edit_event_imageButton);
        this.attendeesForm = view.findViewById(R.id.add_edit_event_Attendees);
        this.eventNameForm = view.findViewById(R.id.add_edit_event_Name);
        this.eventDescriptionForm = view.findViewById(R.id.add_edit_event_description);
        this.profileImageView = view.findViewById(R.id.add_edit_event_ImageView);
        this.progressBar = view.findViewById(R.id.progressBar);
        this.dateEditText = view.findViewById(R.id.add_edit_event_Date);
        this.timeEditText = view.findViewById(R.id.add_edit_event_Time);

        imageController = new ImageController();
        userController = new UserController(view.getContext());
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
                eventController.getAllEventIds(new EventIDsFetchCallback() {
                    @Override
                    public void onSuccess(List<String> EventIDs) {
                        if (EventIDs.contains(result.getContents().substring(1))) {
                            Toast.makeText(view.getContext(), "QR Code is in use ⚠️", Toast.LENGTH_SHORT).show();
                        } else if (result.getContents().startsWith("https://") || result.getContents().startsWith("www")) {
                            Toast.makeText(view.getContext(), "Invalid QR Code ❌", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(view.getContext(), "QR Code Scanned ✅", Toast.LENGTH_SHORT).show();
                            QrID = result.getContents().substring(1);
                            QrChoice = Boolean.TRUE;
                            progressBar.setVisibility(View.VISIBLE);
                            saveEvent();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getContext(), "Error checking QR Code", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        this.newEvent = new Event(this.Organizer, "", "");

        saveButton.setOnClickListener(v -> {
            if (eventNameForm.getText().toString().isEmpty() ||
                    selectedLocationName == null || selectedLocationName.isEmpty() ||
                    eventDescriptionForm.getText().toString().isEmpty() ||
                    null == imageUri) {
                Toast.makeText(view.getContext(), "Please Input All Required Information", Toast.LENGTH_LONG).show();
            } else {
                QROptionsDialog();
            }
        });

        backButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(AddEventFragment.this);
            navController.popBackStack();
            ((MainActivity) requireActivity()).setNavbarVisibility(true);
        });

        editImageButton.setOnClickListener(v -> openGallery());

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY);
        }
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    selectedLocationName = place.getName();
                    LatLng latLng = place.getLatLng();
                    if (latLng != null) {
                        selectedLocationName = place.getName();
                        locationCoords = place.getLatLng().latitude + "," + place.getLatLng().longitude;
                        newEvent.setLocationCoords(locationCoords);

                        View autocompleteView = autocompleteFragment.getView();
                        if (autocompleteView != null) {
                            for (int i = 0; i < ((ViewGroup) autocompleteView).getChildCount(); i++) {
                                View child = ((ViewGroup) autocompleteView).getChildAt(i);
                                if (child instanceof EditText) {
                                    ((EditText) child).setTextColor(Color.WHITE);
                                    ((EditText) child).setTextSize(16);
                                    break;
                                }
                            }
                        }
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    Log.i(TAG, "An error occurred: " + status);
                }
            });
        }

        dateEditText.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().build();
            datePicker.show(requireActivity().getSupportFragmentManager(), datePicker.toString());
            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selection);
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                SimpleDateFormat dateFormat = new SimpleDateFormat("EE, MMMM dd, yyyy", Locale.getDefault());
                date = dateFormat.format(calendar.getTime());
                dateEditText.setText(date);
            });
        });


        timeEditText.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder().build();
            timePicker.show(requireActivity().getSupportFragmentManager(), timePicker.toString());
            timePicker.addOnPositiveButtonClickListener(dialog -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // "a" will show AM/PM
                time = timeFormat.format(calendar.getTime());
                timeEditText.setText(time);
            });
        });

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
        String fileName = newEvent.getId() + ".jpg";

        imageController.uploadImage(imageUri, folderPath, fileName,
                uri -> System.out.println("Success"),
                e -> Log.e("AddEventFragment", "Upload failed: " + e.getMessage()));
    }

    /**
     * Shows a dialog for selecting QR code generation method.
     */
    private void QROptionsDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Check-in Code Options");
        builder.setIcon(R.drawable.onphone);
        String[] options = {"🤖 Autogenerate Code", "♻️ Reuse Old Code"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    QrChoice = Boolean.TRUE;
                    QrID = null;
                    Toast.makeText(getContext(), "Generated QR Code", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.VISIBLE);
                    saveEvent();
                    break;
                case 1:
                    qrCodeScanner.launch(QrScannerController.getOptions());
                    break;
            }
        });
        builder.show();
    }

    /**
     * Saves event details and image to Firebase, then navigates back.
     */
    private void saveEvent() {
        newEvent.setName(eventNameForm.getText().toString());
        newEvent.setLocation(selectedLocationName);
        newEvent.setDescription(eventDescriptionForm.getText().toString());

        if (attendeesForm.getText().toString().isEmpty()) {
            newEvent.setMaximumAttendees(0L);
        } else {
            newEvent.setMaximumAttendees(Long.parseLong(attendeesForm.getText().toString()));
        }
        newEvent.setPosterURI(imageUri);
        newEvent.setAnnouncementCount(0L);
        newEvent.setLocationCoords(locationCoords);
        newEvent.setDate(date);
        newEvent.setTime(time);
        eventController.addEvent(newEvent, QrID);
        uploadImageToFirebase(imageUri);

        eventController.getEventsByUser(requireView(), new EventFetchByUserCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                for (Event event : events) {
                    if (event.getName().equals(newEvent.getName())) {
                        FirebaseMessaging.getInstance().subscribeToTopic(event.getId() + "org")
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.i(TAG, "Subscribed to Organizer Topic: " + event.getId() + "org");
                                    }
                                });
                    }
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });

        NavController navController = NavHostFragment.findNavController(this);
        navController.popBackStack();
        progressBar.setVisibility(View.GONE);
        ((MainActivity) requireActivity()).setNavbarVisibility(true);
    }
}
