package com.example.scanpal;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collection;

public class EventDetailsFragment extends Fragment {

    private String eventName;
    private String eventDescription;
    private String eventOrganizer;
    private String eventLocation;
    //private Collection<DocumentReference> eventAttendees; not really needed

    //future field here for profile picture
    //future field here for event poster/banner

    /**
     * Empty Constructor
     */
    public EventDetailsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_details, null, false);

        //retrieves all the info about specific event from database
        fetchEventDetails();

        Log.d("fire", "end of onCreate");

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
     * for now takes the hardcoded string which links to an event in the database
     * and fetches all of its details(in future add one param for qr Bitmap)
     */
    void fetchEventDetails() {
        EventController eventController = new EventController(FirebaseFirestore.getInstance());
        FirebaseFirestore db = eventController.getDatabase();

        //once qr stuff works just add a parameter to the method with qr string
        //for now test with hardcoded string linking to CSC meet up event
        CollectionReference eventCollection = db.collection("Events");
        DocumentReference EventDocument = eventCollection.document("oudHiTAO4dN9G86RVF2U");//remove later

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
                        }

                        Log.d("fire", eventName);

                    } else {
                        //System.out.println("Error exist");
                        //eventName = "DOESNT EXIST";
                        // Document does not exist
                        Toast.makeText(getView().getContext(), "Event Doesn't exit ", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Error getting document
                    //System.out.println("Error getting");
                    Toast.makeText(getView().getContext(), "Error retrieving Event", Toast.LENGTH_LONG).show();

                }
            }
        });

    }
}
