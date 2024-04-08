package com.example.scanpal.Controllers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.scanpal.Callbacks.AnnouncementsFetchCallback;
import com.example.scanpal.Callbacks.EventFetchCallback;
import com.example.scanpal.Models.Announcement;
import com.example.scanpal.Models.Event;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnouncementController {
    FirebaseFirestore database;
    FirebaseStorage storage;

    public AnnouncementController() {
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }
    
    /**
     * Creates an announcement object and stores in the DB
     *
     * @param announcement The announcement object to push to DB
     */
    public void createAnnouncement(Announcement announcement) {
        Map<String, Object> announcementMap = new HashMap<>();
        DocumentReference announcementRef = database.collection("Announcements").document(announcement.getEventID() +
                "-" + announcement.getAnnouncementNum());

        announcementMap.put("AnnouncementNum", announcement.getAnnouncementNum());
        announcementMap.put("EventID", announcement.getEventID());
        announcementMap.put("Message", announcement.getMessage());
        announcementMap.put("TimeStamp", announcement.getTimeStamp());
        announcementRef.set(announcement).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("CREATE ANNOUNCEMENT", "announcement Created");
                Log.d("CREATE ANNOUNCEMENT", "Created at: " + announcement.getTimeStamp());
            }
        });
        QrCodeController qrCodeController = new QrCodeController();
        EventController eventController = new EventController(qrCodeController);
        eventController.getEventById(announcement.getEventID(), new EventFetchCallback() {
            @Override
            public void onSuccess(Event event) {
                event.setAnnouncementCount(announcement.getAnnouncementNum());
                Map<String, Object> updates = new HashMap<>();
                updates.put("announcementCount", event.getAnnouncementCount());
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
                        Announcement announcement = new Announcement();
                        announcement.setAnnouncementNum((Long) document.get("announcementNum"));
                        announcement.setEventID((String) document.get("eventID"));
                        announcement.setMessage((String) document.get("message"));
                        announcement.setTimeStamp((String) document.get("timeStamp"));
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
}
