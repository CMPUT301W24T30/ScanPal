package com.example.scanpal.Fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.scanpal.Callbacks.AttendeeAddCallback;
import com.example.scanpal.Callbacks.AttendeeDeleteCallback;
import com.example.scanpal.Callbacks.AttendeeFetchCallback;
import com.example.scanpal.Callbacks.AttendeeSignedUpFetchCallback;
import com.example.scanpal.Callbacks.EventDeleteCallback;
import com.example.scanpal.Callbacks.EventFetchCallback;
import com.example.scanpal.Callbacks.UserFetchCallback;
import com.example.scanpal.Controllers.AnnouncementController;
import com.example.scanpal.Controllers.AttendeeController;
import com.example.scanpal.Controllers.EventController;
import com.example.scanpal.Controllers.ShareEventController;
import com.example.scanpal.Controllers.UserController;
import com.example.scanpal.MapsActivity;
import com.example.scanpal.Models.Announcement;
import com.example.scanpal.Models.Attendee;
import com.example.scanpal.Models.Event;
import com.example.scanpal.Models.User;
import com.example.scanpal.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Fragment for displaying event details. It expects an event ID as an argument
 * and uses it to fetch and display event details from a Firestore database.
 */
public class EventDetailsFragment extends Fragment {
    public User userDetails;
    public Attendee attendee;
    public String attendeeId, eventName, eventID, eventDescription, eventOrganizer, getEventOrganizerUserName, eventLocation, ImageURI, date, time;
    protected AttendeeController attendeeController;
    private ImageView eventPoster, organizerImage;
    private Long eventAnnouncementCount, eventCapacity;
    private MaterialButton joinButton;
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    fetchLocationAndRSVP();
                } else {
                    Toast.makeText(getContext(), "Location access is needed for precise event experience.", Toast.LENGTH_LONG).show();
                }
            });
    private FloatingActionButton eventEditButton;
    private ProgressBar progressBar;

    /**
     * Required empty public constructor for instantiating the fragment.
     */
    public EventDetailsFragment() {
    }

    /**
     * Called when the Fragment is visible to the user.
     * This method checks for any arguments passed to the fragment and updates the RSVP status accordingly.
     */
    @Override
    public void onResume() {
        super.onResume();
        eventID = requireArguments().getString("event_id");
        updateRSVPStatus(eventID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.event_details, container, false);
        eventID = requireArguments().getString("event_id");
        fetchEventDetails(eventID);

        // Initialize UI components and setup event handlers
        FloatingActionButton backButton = view.findViewById(R.id.event_details_backButton);
        eventPoster = view.findViewById(R.id.event_detail_imageView);
        joinButton = view.findViewById(R.id.join_button);
        eventEditButton = view.findViewById(R.id.event_editButton);
        organizerImage = view.findViewById(R.id.organizer_image);
        FloatingActionButton shareButton = view.findViewById(R.id.event_shareButton);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup user and attendee controllers
        UserController userController = new UserController(getContext());
        attendeeController = new AttendeeController(FirebaseFirestore.getInstance());

        if (userController.fetchStoredUsername() != null) {
            userController.getUser(userController.fetchStoredUsername(), new UserFetchCallback() {
                @Override
                public void onSuccess(User user) {
                    userDetails = user;
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(view.getContext(), "Failed to load user details", Toast.LENGTH_LONG).show();
                }
            });
        }

        backButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);
            navController.popBackStack();
        });

        eventEditButton.setOnClickListener(v -> {
            MaterialAlertDialogBuilder OrganizerOptions = new MaterialAlertDialogBuilder(this.requireContext());
            String[] OptionsList = {"⚙️ Edit Event", "📣 Send Announcement", "🗿 View Attendees", "📍 View Map", "✅ Show Check-In Code", "📋 Show Event Details Code", "⚠️ Delete Event"};

            OrganizerOptions.setTitle("Organizer Options")
                    .setIcon(R.drawable.onphone)
                    .setItems(OptionsList, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                // Edit Event
                                navToEditDetails();
                                break;
                            case 1:
                                // Send Announcement
                                newAnnouncement();
                                break;
                            case 2:
                                // View Attendees
                                navToViewAttendees();
                                break;
                            case 3:
                                // View Map
                                navToViewMap();
                                break;
                            case 4:
                                // Show Check-In QR
                                navToShowQr(1);
                                break;
                            case 5:
                                // Show Event Details QR
                                navToShowQr(0);
                                break;
                            case 6:
                                // Delete Event
                                deleteEvent(eventID);
                        }
                    })
                    .show();
        });

        joinButton.setOnClickListener(v -> {
            if (attendee != null) {
                showConfirmationDialog("Cancel RSVP", "Are you sure you want to cancel your RSVP?",
                        () -> attendeeController.deleteAttendee(attendee.getId(), new AttendeeDeleteCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getContext(), "RSVP cancelled successfully.", Toast.LENGTH_SHORT).show();
                                setJoinButton(false);
                                updateRSVPStatus(eventID);
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(getContext(), "Failed to cancel RSVP", Toast.LENGTH_LONG).show();
                            }
                        }), null);
            } else {
                CheckBox checkBox = new CheckBox(getContext());
                checkBox.setText("Include Location in RSVP?");
                // Permission already granted, directly fetch location and RSVP
                showConfirmationDialog("Join Event", "Do you want to signup for this event?",
                        () -> attendeeController.fetchSignedUpUsers(eventID, new AttendeeSignedUpFetchCallback() {
                            @Override
                            public void onSuccess(ArrayList<Attendee> attendees) {
                                int currentCount = attendees.size();//how many people are signed up

                                if (currentCount >= eventCapacity && eventCapacity != 0) {
                                    Toast.makeText(getContext(), "This event is full 😔", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Proceed if event is not full
                                if (checkBox.isChecked()) {
                                    // User wants to include location
                                    checkLocationAndRSVP(true);
                                } else {
                                    // User does not want to include location
                                    proceedWithRSVP(null);
                                }

                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(getContext(), "Failed to Get attendees count", Toast.LENGTH_LONG).show();
                            }

                        }), checkBox);
            }
        });

        shareButton.setOnClickListener(v -> {
            ShareEventController shareEventController = new ShareEventController(getContext());
            shareEventController.shareQrCode(eventID);
        });
        updateRSVPStatus(eventID);
        return view;
    }

    /**
     * Fetches the organizer's details based on the provided user reference.
     * Updates the UI with the organizer's name and profile image.
     *
     * @param userRef Reference to the User document in Firestore.
     */
    private void fetchOrganizer(DocumentReference userRef) {

        if (userDetails.isAdministrator()) {
            eventEditButton.setVisibility(View.VISIBLE);
        }

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot organizerDoc = task.getResult();
                if (organizerDoc.exists()) {
                    String firstName = organizerDoc.getString("firstName");
                    String lastName = organizerDoc.getString("lastName");
                    String profileImage = organizerDoc.getString("photo");

                    String organizerName = firstName + " " + lastName;
                    getEventOrganizerUserName = organizerDoc.getId();

                    if ((userDetails.getUsername().equals(getEventOrganizerUserName))) {
                        eventEditButton.setVisibility(View.VISIBLE);
                    }

                    eventOrganizer = organizerName;
                    Glide.with(EventDetailsFragment.this)
                            .load(profileImage)
                            .apply(new RequestOptions().circleCrop())
                            .into(organizerImage);

                    if (isAdded()) {
                        TextView OrganizerName = requireView().findViewById(R.id.event_orgName);

                        if (OrganizerName != null) {
                            OrganizerName.setText(eventOrganizer);
                        }
                    }
                } else {
                    Log.d("TAG", "Organizer document does not exist");
                }
            }
        });
    }

    /**
     * Fetches and displays the details of the event from Firestore based on the provided event ID.
     * Updates the UI with the event's name, description, location, and image.
     *
     * @param eventID The ID of the event to fetch details for.
     */
    void fetchEventDetails(String eventID) {
        EventController eventController = new EventController();
        FirebaseFirestore db = eventController.getDatabase();

        CollectionReference eventCollection = db.collection("Events");
        DocumentReference EventDocument = eventCollection.document(eventID);

        EventDocument.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    eventName = document.getString("name");
                    eventDescription = document.getString("description");
                    eventLocation = document.getString("location");
                    date = document.getString("date");
                    time = document.getString("time");
                    ImageURI = document.getString("photo");
                    eventAnnouncementCount = document.getLong("announcementCount");
                    eventCapacity = document.getLong("capacity");

                    //since user technically another document
                    fetchOrganizer(Objects.requireNonNull(document.getDocumentReference("organizer")));

                    if (isAdded()) {
                        MaterialTextView eventTitle = requireView().findViewById(R.id.event_Title);
                        MaterialTextView eventDes = requireView().findViewById(R.id.event_description);
                        MaterialTextView eventLoc = requireView().findViewById(R.id.event_Location);
                        MaterialTextView eventDate = requireView().findViewById(R.id.event_detail_date);
                        MaterialTextView eventTime = requireView().findViewById(R.id.event_detail_time);

                        if (eventTitle != null) {
                            eventTitle.setText(eventName);
                        }
                        if (eventDes != null) {
                            eventDes.setText(eventDescription);
                        }
                        if (eventDate != null) {
                            eventDate.setText(date);
                        }
                        if (eventTime != null) {
                            eventTime.setText(time);
                        }
                        if (eventLoc != null) {
                            eventLoc.setText(eventLocation);
                        }
                        if (ImageURI != null) {
                            Uri imageURI = Uri.parse(ImageURI);
                            Glide.with(requireView())
                                    .load(imageURI)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .error("https://media1.tenor.com/m/s7Tf_aL-Di0AAAAC/chipi-chipi-chapa-chapa.gif")
                                    .skipMemoryCache(true)
                                    .apply(new RequestOptions().placeholder(R.drawable.ic_launcher_background))
                                    .into(eventPoster);
                        }
                    }
                } else {
                    Toast.makeText(requireView().getContext(), "Event Doesn't exist ", Toast.LENGTH_LONG).show();
                    //TODO: navigate back to / stay on events Page then
                }
            } else {
                Toast.makeText(requireView().getContext(), "Error retrieving Event", Toast.LENGTH_LONG).show();
                //TODO: navigate back to / stay on events Page then
            }
        });
    }

    /**
     * Updates the RSVP status of the current user for the event.
     * Checks if the user has already RSVP'd to the event and updates the join button state accordingly.
     *
     * @param eventID The ID of the event to check RSVP status for.
     */
    private void updateRSVPStatus(String eventID) {
        if (eventID != null && userDetails != null) {
            attendeeId = userDetails.getUsername() + eventID;
            attendeeController.fetchAttendee(attendeeId, new AttendeeFetchCallback() {
                @Override
                public void onSuccess(Attendee existingAttendee) {
                    attendee = existingAttendee;
                    setJoinButton(attendee.isRsvp());
                }

                @Override
                public void onError(Exception e) {
                    attendee = null;
                    setJoinButton(false);
                }
            });
        }
    }

    /**
     * Shows a confirmation dialog with a given title and message.
     * Executes a Runnable if the user confirms the action.
     *
     * @param title     The title of the confirmation dialog.
     * @param message   The message displayed in the dialog.
     * @param onConfirm A Runnable to execute if the user confirms the action.
     */
    private void showConfirmationDialog(String title, String message, Runnable onConfirm, @Nullable CheckBox checkBox) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(title);
        builder.setMessage(message);

        if (checkBox != null) {
            // Set up a LinearLayout for the checkbox to add margins
            LinearLayout container = new LinearLayout(getContext());
            container.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            int leftMarginInPixels = (int) (16 * getResources().getDisplayMetrics().density);
            params.setMargins(leftMarginInPixels, 0, 0, 0);
            checkBox.setLayoutParams(params);
            checkBox.setChecked(true);

            container.addView(checkBox);
            builder.setView(container);
        }

        builder.setPositiveButton("✅ Yes", (dialog, which) -> {
            if (checkBox != null && checkBox.isChecked()) {
                checkLocationAndRSVP(true);
            } else {
                onConfirm.run();
            }
        });
        builder.setNegativeButton("❌ No", null);
        builder.setIcon(R.drawable.onphone);
        builder.show();

    }

    /**
     * Sets the state of the join button based on the user's RSVP status.
     * Changes the button text and background color accordingly.
     *
     * @param isRsvp True if the user has RSVP'd, false otherwise.
     */
    private void setJoinButton(boolean isRsvp) {
        if (isRsvp) {
            joinButton.setText(R.string.cancel_rsvp);
            joinButton.setBackgroundColor(Color.RED);
        } else {
            joinButton.setText(R.string.join_event);
            joinButton.setBackgroundColor(Color.parseColor("#0D6EFD"));
        }
    }

    /**
     * A function call that will navigate to the edit events page if conditions are met
     **/
    void navToEditDetails() {
        if (userDetails.getUsername().equals(getEventOrganizerUserName) ||
                userDetails.isAdministrator()) {
            Bundle bundle = new Bundle();
            bundle.putString("event_id", eventID);
            NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);
            navController.navigate(R.id.edit_existing_event, bundle);
        }
    }


    /**
     * Navigates to the view attendees screen.
     * This method retrieves the NavController associated with the EventDetailsFragment and navigates to the
     * view_signed_up_users destination with the event_id parameter bundled.
     */
    void navToViewAttendees() {
        NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);
        Bundle bundle = new Bundle();
        bundle.putString("event_id", eventID);
        navController.navigate(R.id.view_signed_up_users, bundle);
    }


    /**
     * Navigates to the view map screen.
     * This method creates an intent to navigate to the MapsActivity and adds the event_id parameter to it.
     */
    void navToViewMap() {
        Log.d("EventDetailsFragment", "Sending eventID to MapActivity: " + eventID);
        Intent intent = new Intent(getActivity(), MapsActivity.class);
        String eventID = requireArguments().getString("event_id");
        intent.putExtra("event_id", eventID);
        startActivity(intent);
    }


    /**
     * Navigates to the ShowQrFragment with the specified request type.
     * This method retrieves the NavController associated with the EventDetailsFragment and navigates to the
     * ShowQrFragment destination with the event_id, request, and eventName parameters bundled based on the provided type.
     * If the type is 1, it navigates with a request for check-in QR; otherwise, it navigates with a request for event details QR.
     *
     * @param type The type of request (1 for check-in QR, 0 for event details QR).
     */
    void navToShowQr(int type) {
        if (type == 1) { //check in qr
            NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);
            Bundle bundle = new Bundle();
            bundle.putString("event_id", eventID);
            bundle.putString("request", "check-in");
            bundle.putString("eventName", eventName);
            navController.navigate(R.id.action_eventDetailsPage_to_ShowQrFragment, bundle);

        } else { // details qr
            NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);
            Bundle bundle = new Bundle();
            bundle.putString("event_id", eventID);
            bundle.putString("request", "event");
            bundle.putString("eventName", eventName);
            navController.navigate(R.id.action_eventDetailsPage_to_ShowQrFragment, bundle);
        }
    }

    /**
     * This function creates a dialog box so that the organizer may send an announcement
     * to their attendees
     **/
    void newAnnouncement() {
        EditText messageBox = new EditText(this.getContext());
        MaterialAlertDialogBuilder announcementDialog = new MaterialAlertDialogBuilder(this.requireContext());
        announcementDialog.setView(messageBox);
        EventController eventController = new EventController();

        eventController.getEventById(eventID, new EventFetchCallback() {
            @Override
            public void onSuccess(Event event) {
                eventAnnouncementCount = event.getAnnouncementCount();
            }

            @Override
            public void onError(Exception e) {
            }
        });

        announcementDialog.setTitle("Event Announcement");
        announcementDialog.setIcon(R.drawable.onphone);
        announcementDialog.setPositiveButton("📣 Send", (dialog, which) -> {
            if (messageBox.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Error: Can't make empty Announcement", Toast.LENGTH_LONG).show();
                dialog.cancel();
                return;
            }

            AnnouncementController AC = new AnnouncementController();
            Announcement announcement = new Announcement();
            announcement.setMessage(messageBox.getText().toString());
            announcement.setEventID(eventID);
            announcement.setAnnouncementNum(eventAnnouncementCount + 1L);

            //triggers the cloud functions to send push notifications
            AC.createAnnouncement(announcement);
            Toast.makeText(getContext(), "Announcement sent! 🎉", Toast.LENGTH_LONG).show();
        });
        announcementDialog.setNegativeButton("❌ Cancel", (dialog, which) -> dialog.cancel());
        announcementDialog.show();
    }

    /**
     * Fetches the current location and proceeds with RSVP.
     * This method uses the FusedLocationProviderClient to fetch the last known location.
     * If the ACCESS_FINE_LOCATION permission is granted, it retrieves the last known location
     * and proceeds to RSVP with the location information. If the location is not available,
     * it proceeds with RSVP without location information.
     */
    private void fetchLocationAndRSVP() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    // Got the location, now proceed to RSVP with location information
                    String locationStr = location.getLatitude() + "," + location.getLongitude();
                    proceedWithRSVP(locationStr);
                } else {
                    // Couldn't get the location, proceed without it
                    proceedWithRSVP(null);
                }
            });
        }
    }


    /**
     * Checks for location permission and proceeds with RSVP.
     * This method checks if the ACCESS_FINE_LOCATION permission is granted. If not, it requests
     * the permission using locationPermissionLauncher. If the permission is already granted,
     * it directly fetches the location and proceeds with RSVP.
     *
     * @param includeLocation
     */
    private void checkLocationAndRSVP(boolean includeLocation) {
        if (includeLocation) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, request it
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            } else {
                // Permission already granted, directly fetch location and RSVP
                fetchLocationAndRSVP();
            }
        } else {
            // Proceed with RSVP without including location
            proceedWithRSVP(null);
        }
    }


    /**
     * Proceeds with RSVP for the event.
     * This method creates an attendee object with the user details, event ID, and RSVP status.
     * If locationStr is not null, it sets the location for the attendee. Then, it adds the attendee
     * to the database using the attendeeController. After a successful RSVP, it subscribes the user
     * to the event's topic for notifications and updates the RSVP status UI.
     *
     * @param locationStr The location string to be associated with the attendee, can be null.
     */
    private void proceedWithRSVP(@Nullable String locationStr) {
        if (eventID != null && userDetails != null) {
            String attendeeId = userDetails.getUsername() + eventID;
            attendee = new Attendee(userDetails, eventID, true, false, 0L);
            attendee.setId(attendeeId);
            if (locationStr != null) {
                attendee.setLocation(locationStr);
            }

            attendeeController.addAttendee(attendee, new AttendeeAddCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "RSVP successful.", Toast.LENGTH_SHORT).show();
                    FirebaseMessaging.getInstance().subscribeToTopic(eventID).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            setJoinButton(true);
                        }
                    });
                    updateRSVPStatus(eventID);
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getContext(), "Failed to RSVP", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Deletes the event after confirming the action with the user.
     * Displays a confirmation dialog to the user, and if confirmed, proceeds to delete the event
     * using the eventController and attendeeController to clean up associated data.
     */
    private void deleteEvent(String eventID) {
        EventController eventController = new EventController();
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Event?")
                .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
                .setIcon(R.drawable.danger_icon)
                .setPositiveButton("Delete", (dialog, whichButton) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    eventController.deleteEvent(eventID, new EventDeleteCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);
                            navController.popBackStack();
                        }

                        @Override
                        public void onError(Exception e) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                })
                .setNegativeButton("Cancel", null).show();
    }
}