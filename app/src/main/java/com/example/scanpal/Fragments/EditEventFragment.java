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

import com.bumptech.glide.Glide;
import com.example.scanpal.BuildConfig;
import com.example.scanpal.Callbacks.EventFetchCallback;
import com.example.scanpal.Callbacks.EventUpdateCallback;
import com.example.scanpal.Controllers.EventController;
import com.example.scanpal.MainActivity;
import com.example.scanpal.Models.Event;
import com.example.scanpal.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

/**
 * Fragment for editing the details of an existing event.
 * Allows the user to update event information such as name, location, description,
 * and maximum attendees. It also enables image editing for the event and provides
 * an option to delete the event.
 */
public class EditEventFragment extends Fragment {
    private Button editImageButton;
    private FloatingActionButton backButton, saveButton;
    private EditText eventNameForm, eventDescriptionForm;
    private TextInputEditText dateForm, timeForm, attendeesForm;
    private TextView pageHeader;
    private ImageView eventImageView;
    private Uri newImageUri, existingImageUri;
    protected ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    newImageUri = result.getData().getData();
                    Glide.with(this)
                            .load(newImageUri)
                            .into(eventImageView);
                }
            });
    private Long announcementCount;
    private ProgressBar progressBar;
    private PlacesClient placesClient;
    private EventController eventController;
    private String eventID, eventLocation, locationCoords, date, time;
    private AutocompleteSupportFragment autocompleteFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_edit_event, container, false);
        ((MainActivity) requireActivity()).setNavbarVisibility(false);
        initializeUI(view);
        pageHeader.setText("Edit ⚙️");

        eventController = new EventController();
        eventID = requireArguments().getString("event_id");
        placesClient = Places.createClient(requireActivity());

        setupEventDetails(eventID);
        setListeners();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    eventLocation = place.getName();
                    LatLng latLng = place.getLatLng();
                    if (latLng != null) {
                        eventLocation = place.getName();
                        locationCoords = place.getLatLng().latitude + "," + place.getLatLng().longitude;
                    }
                    setAutocompleteTextColor();
                }

                @Override
                public void onError(@NonNull Status status) {
                }
            });
        }

        // delay to ensure color change is after fragment loads
        view.postDelayed(this::setAutocompleteTextColor, 100);
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
        eventNameForm = view.findViewById(R.id.add_edit_event_Name);
        dateForm = view.findViewById(R.id.add_edit_event_Date);
        timeForm = view.findViewById(R.id.add_edit_event_Time);
        eventDescriptionForm = view.findViewById(R.id.add_edit_event_description);
        attendeesForm = view.findViewById(R.id.add_edit_event_Attendees);
        eventImageView = view.findViewById(R.id.add_edit_event_ImageView);
        progressBar = view.findViewById(R.id.progressBar);
        autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY);
    }


    /**
     * Sets the text color for the autocomplete view.
     * This method retrieves the autocomplete view associated with the autocomplete fragment. If the view exists,
     * it iterates through its child views to find the EditText view responsible for displaying the text. Once found,
     * it sets the text color to white.
     */
    private void setAutocompleteTextColor() {
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


    /**
     * Sets up listeners for the various buttons in the fragment.
     * This includes listeners for saving changes, navigating back, editing the image,
     * and deleting the event.
     */
    private void setListeners() {
        editImageButton.setOnClickListener(v -> openGallery());
        saveButton.setOnClickListener(v -> saveEventChanges());
        backButton.setOnClickListener(v -> navigateBack());
        dateForm.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().build();
            datePicker.show(requireActivity().getSupportFragmentManager(), datePicker.toString());
            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selection);
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                SimpleDateFormat dateFormat = new SimpleDateFormat("EE, MMMM dd, yyyy", Locale.getDefault());
                date = dateFormat.format(calendar.getTime());
                dateForm.setText(date);
            });
        });

        timeForm.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H).build();
            timePicker.show(requireActivity().getSupportFragmentManager(), timePicker.toString());
            timePicker.addOnPositiveButtonClickListener(dialog -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // "a" will show AM/PM
                time = timeFormat.format(calendar.getTime());
                timeForm.setText(time);
            });
        });

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
                Log.d("EditEventFragment", "Fetched location coords: " + event.getLocationCoords());
                eventNameForm.setText(event.getName());
                autocompleteFragment.setText(event.getLocation());
                eventDescriptionForm.setText(event.getDescription());
                dateForm.setText(event.getDate());
                timeForm.setText(event.getTime());

                // otherwise eventLocation field stays null and you cant save the event without re-selecting location
                eventLocation = event.getLocation();
                locationCoords = event.getLocationCoords();
                Log.d("LocationLog", "Location Coordinates: " + event.getLocationCoords());

                if (event.getMaximumAttendees() != 0) {
                    attendeesForm.setText(String.valueOf(event.getMaximumAttendees()));
                }

                existingImageUri = event.getPosterURI();
                announcementCount = event.getAnnouncementCount();

                MaterialSwitch trackLocationSwitch = requireView().findViewById(R.id.track_location_switch);
                trackLocationSwitch.setChecked(event.isTrackLocation());

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

        if (eventName.isEmpty() || eventLocation == null || eventDescription.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);
        editImageButton.setEnabled(false);

        Event event = new Event(null, eventName, eventDescription);
        event.setLocation(eventLocation);
        event.setLocationCoords(locationCoords);
        event.setDate(dateForm.getText().toString());
        event.setTime(timeForm.getText().toString());

        if (attendeesForm.getText().toString().isEmpty()) {
            event.setMaximumAttendees(0L); // treat zero as 'no limit'
        } else {
            Long maxAttendees = Long.parseLong(attendeesForm.getText().toString());
            event.setMaximumAttendees(maxAttendees);
        }

        event.setId(eventID);
        event.setPosterURI(existingImageUri);
        event.setAnnouncementCount(announcementCount);

        eventController.editEvent(event, newImageUri, new EventUpdateCallback() {
            @Override
            public void onSuccess(boolean status) {
                progressBar.setVisibility(View.GONE);
                navigateBack();
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
            }
        });
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
    private void navigateBack() {
        if (isAdded()) {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
            ((MainActivity) requireActivity()).setNavbarVisibility(true);
        }
    }
}
