package com.example.scanpal.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.example.scanpal.Callbacks.AttendeeAddCallback;
import com.example.scanpal.Callbacks.AttendeeDeleteCallback;
import com.example.scanpal.Callbacks.AttendeeFetchCallback;
import com.example.scanpal.Callbacks.AttendeeSignedUpFetchCallback;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    public String attendeeId, eventName, eventID, eventDescription, eventOrganizer, getEventOrganizerUserName, eventLocation, ImageURI;
    public AttendeeController attendeeController;
    private ImageView eventPoster, organizerImage;
    private Long eventAnnouncementCount, eventCapacity;
    private MaterialButton joinButton;
    private FloatingActionButton eventEditButton;

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

        // Setup user and attendee controllers
        UserController userController = new UserController(FirebaseFirestore.getInstance(), getContext());
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
            String[] OptionsList = {"Edit Event", "Send Announcement", "View Attendees", "View Map", "Show Check-In QR", "Show Event Details QR"};

            OrganizerOptions.setTitle("Organizer Options")
                    .setItems(OptionsList, (dialog, which) -> {

                        //might add more options later?
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
                                onResume();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(getContext(), "Failed to cancel RSVP", Toast.LENGTH_LONG).show();
                            }
                        }));
            } else {
                showConfirmationDialog("Join Event", "Do you want to signup for this event?",
                        () -> attendeeController.fetchSignedUpUsers(eventID, new AttendeeSignedUpFetchCallback() {
                            @Override
                            public void onSuccess(ArrayList<Attendee> attendees) {
                                int currentCount = attendees.size();//how many people are signed up

                                if (currentCount >= eventCapacity && eventCapacity != 0) {
                                    Toast.makeText(getContext(), "This event is full 😔", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (eventID != null && userDetails != null) {
                                    String attendeeId = userDetails.getUsername() + eventID;
                                    attendee = new Attendee(userDetails, eventID, true, false,0);//zero cause new object
                                    attendee.setId(attendeeId);
                                    attendeeController.addAttendee(attendee, new AttendeeAddCallback() {
                                        @Override
                                        public void onSuccess() {
                                            Toast.makeText(getContext(), "RSVP successful.", Toast.LENGTH_SHORT).show();
                                            FirebaseMessaging.getInstance().subscribeToTopic(eventID)
                                                    .addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {
                                                            setJoinButton(true);
                                                            onResume();
                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Toast.makeText(getContext(), "Failed to RSVP", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(getContext(), "Failed to Get attendees count", Toast.LENGTH_LONG).show();
                            }
                        }));
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

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot organizerDoc = task.getResult();
                if (organizerDoc.exists()) {
                    String firstName = organizerDoc.getString("firstName");
                    String lastName = organizerDoc.getString("lastName");
                    String profileImage = organizerDoc.getString("photo");

                    String organizerName = firstName + " " + lastName;
                    getEventOrganizerUserName = organizerDoc.getId();

                    if ((userDetails.getUsername().equals(getEventOrganizerUserName)) ||
                            (userDetails.getUsername().equals(getEventOrganizerUserName)) && (userDetails.isAdministrator())) {
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
                    ImageURI = document.getString("photo");
                    eventAnnouncementCount = document.getLong("announcementCount");
                    eventCapacity = document.getLong("capacity");

                    //since user technically another document
                    fetchOrganizer(Objects.requireNonNull(document.getDocumentReference("organizer")));

                    if (isAdded()) {
                        TextView eventTitle = requireView().findViewById(R.id.event_Title);
                        TextView eventDes = requireView().findViewById(R.id.event_description);
                        TextView eventLoc = requireView().findViewById(R.id.event_Location);

                        if (eventTitle != null) {
                            eventTitle.setText(eventName);
                        }
                        if (eventDes != null) {
                            eventDes.setText(eventDescription);
                        }
                        if (eventLoc != null) {
                            eventLoc.setText(eventLocation);
                        }
                        if (eventPoster != null) {
                            Uri imageURI = Uri.parse(ImageURI);
                            Glide.with(requireView())
                                    .load(imageURI)
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
    private void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("✅ Yes", (dialog, which) -> onConfirm.run())
                .setNegativeButton("❌ No", null)
                .setIcon(R.drawable.onphone)
                .show();
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

    void navToViewAttendees() {
        NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);
        Bundle bundle = new Bundle();
        bundle.putString("event_id", eventID);
        navController.navigate(R.id.view_signed_up_users, bundle);
    }

    void navToViewMap() {
        Log.d("EventDetailsFragment", "Sending eventID to MapActivity: " + eventID);
        Intent intent = new Intent(getActivity(), MapsActivity.class);
        String eventID = requireArguments().getString("event_id");
        intent.putExtra("event_id", eventID);
        startActivity(intent);
    }

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

        announcementDialog.setPositiveButton("Send", (dialog, which) -> {
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
            AC.createAnnouncment(announcement);
            Toast.makeText(getContext(), "Announcement sent!", Toast.LENGTH_LONG).show();
        });
        announcementDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        announcementDialog.show();
    }
}
