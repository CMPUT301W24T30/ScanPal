package com.example.scanpal;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnouncementController {
    FirebaseFirestore database;
    FirebaseStorage storage;

    AnnouncementController() {
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }


    /**
     * Creates an announcement object and stores in the DB
     * @param announcement The announcement object to push to DB
     *
     */
    public void createAnnouncment(Announcement announcement) {
        Map<String, Object> announcementMap = new HashMap<>();
        DocumentReference announcementRef = database.collection("Announcements").document(announcement.getEventID() +
                "-" + announcement.getAnnouncementNum() );//new ID for announcement

        announcementMap.put("AnnouncementNum", announcement.getAnnouncementNum());
        announcementMap.put("EventID", announcement.getEventID());
        announcementMap.put("Message", announcement.getMessage());
        announcementMap.put( "TimeStamp", announcement.getTimeStamp() );

        announcementRef.set(announcement).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                Log.d("CREATE ANNOUNCEMENT", "announcement Created");
                Log.d("CREATE ANNOUNCEMENT", "Created at: " + announcement.getTimeStamp());
            }
        });

        //should update the referenced event incrementing its announcement count

        EventController eventController = new EventController();

        eventController.getEventById(announcement.getEventID(), new EventFetchCallback() {
            @Override
            public void onSuccess(Event event) {
                //theEvent = event;
                event.setAnnouncementCount(announcement.getAnnouncementNum());

                Map<String, Object> updates = new HashMap<>();
                updates.put("announcementCount", event.getAnnouncementCount()); //TODO: TEST FOR BUGS LATER

                //eventController.editEventById(announcement.getEventID(),event);

                DocumentReference eventRef = database.collection("Events").document(announcement.getEventID());

                eventRef
                        .update(updates)
                        .addOnSuccessListener(unused -> Log.d("DB", "Document successfully updated!")).addOnFailureListener(e -> Log.w("DB ERROR", "Error updating document", e));


            }

            @Override
            public void onError(Exception e) {

            }
        });

    }

    public void getAnnouncementsByEventId(String EventID, AnnouncementsFetchCallback callback) {
        CollectionReference announcementsRef = database.collection("Announcements");


        Query query = announcementsRef.whereEqualTo("eventID", EventID);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Announcement> announcementsList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        Announcement announcement = new Announcement();//document.toObject(Announcement.class);
                        announcement.setAnnouncementNum((long) document.get("announcementNum"));
                        announcement.setEventID((String) document.get("eventID"));
                        announcement.setMessage((String) document.get("message"));
                        announcement.setTimeStamp((String) document.get("timeStamp"));


                        // Add the Announcement object to the list
                        announcementsList.add(announcement);

                    }
                    callback.onSuccess(announcementsList);

                    for (Announcement announcement : announcementsList) {
                        Log.d("Announcement", announcement.toString());
                    }
                } else {
                    Log.d("Firestore", "Error getting documents: ", task.getException());
                }
            }
        });

    }

    /**
     * Send a push notification to the devices of users tied to the eventID
     * in the announcement object
     * @param announcement contains the details in the push notification
     */
    public void pushNotificationToAttendees(Announcement announcement) {

    }


    //TODO: Milestone alert method for organizers
}
