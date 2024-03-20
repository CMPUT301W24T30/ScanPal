package com.example.scanpal;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Objects;

/**
 * Fragment for displaying event details. It expects an event ID as an argument
 * and uses it to fetch and display event details from a Firestore database.
 */
public class EventDetailsFragment extends Fragment {

    public User userDetails;
    public Attendee attendee;
    public String attendeeId;
    public AttendeeController attendeeController;
    // ActivityResultLauncher for QR code scanner
    ActivityResultLauncher<ScanOptions> qrCodeScanner;
    private QrScannerController qrScannerController;
    private String eventName;
    private String eventID;
    private String eventDescription;
    private String eventOrganizer;
    private String getEventOrganizerUserName;
    private String eventLocation;
    private ImageView eventPoster;
    private Long eventAnnouncementCount;
    private String ImageURI;
    private Button joinButton;
    private FloatingActionButton eventEditButton;
    private ImageView organizerImage;
    private Button viewSignedUpUsersBtn;

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
        if (getArguments() != null) {
            String eventID = getArguments().getString("event_id");
            updateRSVPStatus(eventID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_details, null, false);
        assert getArguments() != null;
        eventID = getArguments().getString("event_id");
        fetchEventDetails(eventID);


        // Initialize UI components and setup event handlers
        FloatingActionButton backButton = view.findViewById(R.id.event_details_backButton);
        eventPoster = view.findViewById(R.id.event_detail_imageView);
        joinButton = view.findViewById(R.id.join_button);
        eventEditButton = view.findViewById(R.id.event_editButton);
        organizerImage = view.findViewById(R.id.organizer_image);
        FloatingActionButton shareButton = view.findViewById(R.id.event_shareButton);
        viewSignedUpUsersBtn = view.findViewById(R.id.view_signed_up_users_button);

        // Setup user and attendee controllers
        UserController userController = new UserController(FirebaseFirestore.getInstance(), getContext());
        attendeeController = new AttendeeController(FirebaseFirestore.getInstance(), getContext());
        System.out.println("test");
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

        System.out.println("test");

        // Navigate back to the events page
        backButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);
            navController.navigate(R.id.eventsPage);
        });

        // Implement event edit functionality
        eventEditButton.setOnClickListener(v -> {

            AlertDialog.Builder OrganizerOptions = new AlertDialog.Builder(this.getContext());

            // Set the message show for the Alert time
            OrganizerOptions.setMessage("Edit Details or send announcement?");

            // Set Alert Title
            OrganizerOptions.setTitle("Organizer Options");

            // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
            OrganizerOptions.setPositiveButton("Send Announcement", (DialogInterface.OnClickListener) (dialog, which) -> {
                // When the user click yes button then app will close
                //finish();
                newAnnouncement(view);
            });

            // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
            OrganizerOptions.setNegativeButton("Edit Event Details", (DialogInterface.OnClickListener) (dialog, which) -> {
                // If user click no then dialog box is canceled.
                //dialog.cancel();
                navToEditDetails(view);
            });

            OrganizerOptions.show();

            // Check if the current user is the organizer or an admin
            //navToEditDetails(view);
        });

        System.out.println("test");

        System.out.println("test");

        joinButton.setOnClickListener(v -> {
            if (attendee != null) {
                showConfirmationDialog("Cancel RSVP", "Are you sure you want to cancel your RSVP?",
                        () -> attendeeController.deleteAttendee(attendee.getId(), new AttendeeDeleteCallback() {
                            @Override
                            public void onSuccess() {

                                //TODO: unsubscribe user from topic in FCM
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
                showConfirmationDialog("Join Event", "Do you want to RSVP to this event?",
                        () -> {
                            if (eventID != null && userDetails != null) {
                                String attendeeId = userDetails.getUsername() + eventID;
                                attendee = new Attendee(userDetails, eventID, true, false);
                                attendee.setId(attendeeId);
                                attendeeController.addAttendee(attendee, new AttendeeAddCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(getContext(), "RSVP successful.", Toast.LENGTH_SHORT).show();

                                        FirebaseMessaging.getInstance().subscribeToTopic(eventID)
                                                .addOnCompleteListener(task -> {
                                                    String msg = "Subscribed";
                                                    if (!task.isSuccessful()) {
                                                        msg = "Subscribe failed";

                                                    } else {
                                                        setJoinButton(true);
                                                        onResume();
                                                        Log.d("Subscribing", msg);
                                                    }

                                                    //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                                });

                                        //setJoinButton(true);
                                        //onResume();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(getContext(), "Failed to RSVP", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
            }
        });

        System.out.println("test");

        viewSignedUpUsersBtn.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);
            Bundle bundle = new Bundle();
            bundle.putString("eventID", eventID);
            navController.navigate(R.id.view_signed_up_users, bundle);
        });


//        // Share button functionality
//        shareButton.setOnClickListener(v -> {
//            ShareEventController shareEventController = new ShareEventController(getContext());
//            shareEventController.shareQrCode(eventID);
//        });

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

                    //Hide edit button?
                    if (!(userDetails.getUsername().equals(getEventOrganizerUserName)) ||
                            !(userDetails.getUsername().equals(getEventOrganizerUserName)) && !(userDetails.isAdministrator())) {
                        eventEditButton.hide(); //just hide the edit button
                    }

                    eventOrganizer = organizerName;
                    Glide.with(EventDetailsFragment.this)
                            .load(profileImage)
                            .apply(new RequestOptions().circleCrop())
                            .into(organizerImage);

                    // 'isAdded' is necessary for async task purposes
                    if (isAdded()) {
                        TextView OrganizerName = requireView().findViewById(R.id.event_orgName);

                        if (OrganizerName != null) {
                            OrganizerName.setText(eventOrganizer);
                        }

                        Log.d("EventDetailsFragment", userDetails.getUsername());
                        Log.d("EventDetailsFragment", getEventOrganizerUserName);
                        if (getEventOrganizerUserName.contentEquals(userDetails.getUsername())) {
                            viewSignedUpUsersBtn.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    Log.d("TAG", "Organizer document does not exist");
                }
            } else {
                Log.d("TAG", "Failed to get organizer document", task.getException());
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

                    //since user technically another document
                    fetchOrganizer(Objects.requireNonNull(document.getDocumentReference("organizer")));

                    if (isAdded()) {
                        TextView eventTitle = requireView().findViewById(R.id.event_Title);
                        TextView OrganizerName = requireView().findViewById(R.id.event_orgName);
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

                    // Document does not exist

                    Toast.makeText(requireView().getContext(), "Event Doesn't exist ", Toast.LENGTH_LONG).show();
                    //TODO: navigate back to / stay on events Page then
                }
            } else {
                // Error getting document
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
                .setPositiveButton(android.R.string.yes, (dialog, which) -> onConfirm.run())
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
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
            joinButton.setText("Cancel RSVP");
            joinButton.setBackgroundColor(Color.RED);
        } else {
            joinButton.setText("Join Event");
            joinButton.setBackgroundColor(Color.parseColor("#0D6EFD"));
        }
    }

    /**
     * A function call that will navigate to the edit events page if conditions are met
     *
     * @param view The current view
     */
    void navToEditDetails(View view) {
        if (userDetails.getUsername().equals(getEventOrganizerUserName) ||
                userDetails.isAdministrator()) {
            // Navigate to the event edit page with necessary details
            NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);

            Bundle bundle = new Bundle();
            bundle.putString("0", eventID);
            bundle.putString("1", eventName);
            bundle.putString("2", eventLocation);
            bundle.putString("3", eventDescription);
            bundle.putString("4", ImageURI);

            navController.navigate(R.id.edit_existing_event, bundle);
        } else {
            Toast.makeText(view.getContext(), "You Cannot Edit This Event", Toast.LENGTH_LONG).show();
            // should just hide the button here instead
        }
    }

    /**
     * This function creates a dialogbox so that the organizer may send an announcement
     * to their attendees
     *
     * @param view The current view
     */
    void newAnnouncement(View view) {
        EditText messageBox = new EditText(this.getContext());
        AlertDialog.Builder announcementDialog = new AlertDialog.Builder(this.getContext());
        announcementDialog.setView(messageBox);

        // Set the message show for the Alert time
        //announcementDialog.setMessage("");

        EventController eventController = new EventController();


        eventController.getEventById(eventID, new EventFetchCallback() {
            @Override
            public void onSuccess(Event event) {
                eventAnnouncementCount = event.getAnnouncementCount();//incase multiple announcements at a time
            }

            @Override
            public void onError(Exception e) {

            }
        });

        // Set Alert Title
        announcementDialog.setTitle("Event Announcement");

        announcementDialog.setPositiveButton("Send", (DialogInterface.OnClickListener) (dialog, which) -> {
            if (messageBox.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Error: Can't make empty Announcement", Toast.LENGTH_LONG).show();
                dialog.cancel();
                return;//return to prevent creating a new announcement
            }

            AnnouncementController AC = new AnnouncementController();

            Announcement announcement = new Announcement();
            announcement.setMessage(messageBox.getText().toString());
            announcement.setEventID(eventID);
            announcement.setAnnouncementNum(eventAnnouncementCount + 1L);//increment announcement num


            //triggers the cloud functions to send push notifications
            AC.createAnnouncment(announcement);

            Toast.makeText(getContext(), "Announcement sent!", Toast.LENGTH_LONG).show();

        });

        // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
        announcementDialog.setNegativeButton("Cancel", (DialogInterface.OnClickListener) (dialog, which) -> dialog.cancel());

        announcementDialog.show();

    }
}
