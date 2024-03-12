package com.example.scanpal;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Objects;

/**
 * Fragment for displaying event details. It expects an event ID as an argument
 * and uses it to fetch and display event details from a Firestore database.
 */
public class EventDetailsFragment extends Fragment {

    // User details of the current user
    public User userDetails;
    // The attendee of the event
    public User user;
    public Attendee attendee;
    // Attendee ID, constructed from user's username and eventID
    public String attendeeId;
    public AttendeeController attendeeController;
    // ActivityResultLauncher for QR code scanner
    ActivityResultLauncher<ScanOptions> qrCodeScanner;
    private QrScannerController qrScannerController;
    // Event details
    private String eventName;
    private String eventDescription;
    private String eventOrganizer;
    private String getEventOrganizerUserName;
    private String eventLocation;
    private User userDetails;// current user
    private ImageView eventPoster;
    private String ImageURI;

    //private Collection<DocumentReference> eventAttendees; not really needed?

    //TODO: future field here for organizer profile picture?
    //TODO: future field here for event poster/banner?

    /**
     * Empty Constructor
     */
    public EventDetailsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_details, null, false);

        //retrieves all the info about specific event from database
        String eventID =  getArguments().getString("0");
        fetchEventDetails(eventID);

        // Initialize UI components and setup event handlers
        FloatingActionButton backButton = view.findViewById(R.id.event_details_backButton);
        eventPoster = view.findViewById(R.id.event_detail_imageView);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);
                navController.navigate(R.id.eventsPage);
            }
        });

        //Implement Editing Event Details
        eventEditButton = view.findViewById(R.id.event_editButton);
        eventEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if the user is the organizer of this event, if not send toast and do nothing

                UserController userController = new UserController(FirebaseFirestore.getInstance(), view.getContext());

                userController.getUser(userController.fetchStoredUsername(), new UserFetchCallback() {
                    @Override
                    public void onSuccess(User user) {
                        userDetails = user;
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(view.getContext(), "Failed to Edit Event", Toast.LENGTH_LONG).show();

                    }
                });

                if(userDetails.getUsername().equals(getEventOrganizerUserName) ||
                    userDetails.isAdministrator()) {
                    //Allow editing of this events details here
                    NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);


                    //navigate with a bundle containing the eventID,max attendees, name,location,desc,and imgURI
                    //TODO: get maxAttendees

                    Bundle bundle = new Bundle();

                    bundle.putString("0", eventID);
                    bundle.putString("1", eventName);
                    bundle.putString("2", eventLocation);
                    bundle.putString("3", eventDescription);
                    bundle.putString("4", ImageURI);

                    navController.navigate(R.id.edit_existing_event, bundle);//navigate to edit this event

                }
                else {
                    Toast.makeText(view.getContext(), "You Cannot Edit This Event", Toast.LENGTH_LONG).show();

                }

            }
        });

        return view;
    }

    /**
     * This method takes the inputted /User document reference stores their first and
     * lastname as one string in the 'eventOrganizer' field
     *
     * @param userRef this is just the specific 'user' that was found to be tied to the event
     */
    private void fetchOrganizer(DocumentReference userRef) {

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot organizerDoc = task.getResult();
                    if (organizerDoc.exists()) {
                        String firstName = organizerDoc.getString("firstName");
                        String lastName = organizerDoc.getString("lastName");

                        String organizerName = firstName + " " + lastName;

                        getEventOrganizerUserName = organizerDoc.getId().toString();
                        //Log.d("GETTING USERNAME", organizerDoc.getId().toString());

                        eventOrganizer = organizerName;

                        // 'isAdded' is necessary for async task purposes
                        if(isAdded()) {
                            TextView OrganizerName = getView().findViewById(R.id.event_orgName);

                            if (OrganizerName != null) {
                                OrganizerName.setText(eventOrganizer);
                            }
                        }
                    } else {
                        Log.d("TAG", "Organizer document does not exist");
                    }
                } else {
                    Log.d("TAG", "Failed to get organizer document", task.getException());
                }
            }
        });
    }

    /**
     *
     * takes an eventID which links to an event in the database
     * and fetches all of its details
     */
    void fetchEventDetails(String EventID) {
        EventController eventController = new EventController();
        FirebaseFirestore db = eventController.getDatabase();

        CollectionReference eventCollection = db.collection("Events");
        DocumentReference EventDocument = eventCollection.document(EventID);

        EventDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        // Get the actual name from the document
                        eventName = document.getString("name");
                        eventDescription = document.getString("description");
                        eventLocation = document.getString("location");
                        ImageURI = document.getString("photo");

                        //since user technically another document
                        fetchOrganizer(document.getDocumentReference("organizer"));

                        if(isAdded()) {
                            TextView eventTitle = getView().findViewById(R.id.event_Title);
                            TextView OrganizerName = getView().findViewById(R.id.event_orgName);
                            TextView eventDes = getView().findViewById(R.id.event_description);
                            TextView eventLoc = getView().findViewById(R.id.event_Location);

                            if (eventTitle != null) {
                                eventTitle.setText(eventName);
                            }
                            if(eventDes != null) {
                                eventDes.setText(eventDescription);
                            }
                            if(eventLoc != null) {
                                eventLoc.setText(eventLocation);
                            }
                            if(eventPoster != null) {
                                Uri imageURI = Uri.parse(ImageURI);
                                Glide.with(getView())
                                        .load(imageURI)
                                        .apply(new RequestOptions().placeholder(R.drawable.ic_launcher_background))
                                        .into(eventPoster);
                            }
                        }

                    } else {

                        //eventName = "DOESNT EXIST";
                        // Document does not exist

                        Toast.makeText(getView().getContext(), "Event Doesn't exist ", Toast.LENGTH_LONG).show();
                        //TODO: navigate back to / stay on events Page then
                    }
                } else {
                    // Error getting document
                    Toast.makeText(getView().getContext(), "Error retrieving Event", Toast.LENGTH_LONG).show();
                    //TODO: navigate back to / stay on events Page then

                }
            }
        });

    }
}
