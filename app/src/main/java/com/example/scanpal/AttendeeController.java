package com.example.scanpal;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles operations related to attendee management in a Firestore database.
 */
public class AttendeeController {
    private final FirebaseFirestore database; // instance of the database
    private final Context context;

    public AttendeeController(FirebaseFirestore database, Context context) {
        this.database = database;
        this.context = context;
    }

    public FirebaseFirestore getDatabase() {
        return this.database;
    }

    public void test(String attendeeId) {
        Log.wtf("TESTIES", attendeeId);
    }

    /**
     * Adds a new attendee to the Firestore database.
     *
     * @param attendee The attendee to be added to the database.
     */
    public void addAttendee(Attendee attendee, AttendeeAddCallback callback) {

        try {
            FileOutputStream fos = context.openFileOutput(attendee.getId() + ".ser", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(attendee);
            oos.close();
            fos.close();
        } catch (Exception e) {
            callback.onError(e);
        }

        // will be responsible for add all user types to the database
        Map<String, Object> attendeeMap = new HashMap<>();
        attendeeMap.put("location", attendee.getLocation());
        attendeeMap.put("checkedIn", attendee.isCheckedIn());
        attendeeMap.put("rsvp", attendee.isRsvp());

        DocumentReference userRef = database.collection("Users").document(attendee.getUser().getUsername());
        attendeeMap.put("user", userRef);

        DocumentReference eventRef = database.collection("Events").document(attendee.getEventID());
        attendeeMap.put("eventID", eventRef);

        // Save to database
        database.collection("Attendees").document(attendee.getId()).set(attendeeMap)
                .addOnSuccessListener(aVoid -> System.out.println("Attendee added successfully!"))
                .addOnFailureListener(e -> System.out.println("Error adding attendee: " + e.getMessage()));
    }

    /**
     * Fetches an attendee's details from the Firestore database.
     *
     * @param attendeeId The ID of the attendee to fetch.
     * @param callback   A callback interface to handle the response.
     */
    public void fetchAttendee(String attendeeId, AttendeeFetchCallback callback) {

        try {
            FileInputStream fis = context.openFileInput(attendeeId + ".ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            Attendee attendee = (Attendee) ois.readObject();
            ois.close();
            fis.close();
            callback.onSuccess(attendee);
            return;
        } catch (Exception e) {
            // Proceed to fetch from Firestore if local fetch fails
        }

        database.collection("Attendees").document(attendeeId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            String location = documentSnapshot.getString("location");
                            boolean checkedIn = Boolean.TRUE.equals(documentSnapshot.getBoolean("checkedIn"));
                            boolean rsvp = Boolean.TRUE.equals(documentSnapshot.getBoolean("rsvp"));
                            DocumentReference userRef = documentSnapshot.getDocumentReference("user");
                            String eventID = documentSnapshot.getString("eventID");

                        } catch (Exception e) {
                            callback.onError(e);
                        }
                    } else {
                        callback.onError(new Exception("Attendee not found"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }


    /**
     * Updates the RSVP status of an attendee in the Firestore database.
     *
     * @param attendee The attendee object to be updated.
     * @param callback The callback to report success or failure.
     */
    public void updateAttendee(Attendee attendee, AttendeeUpdateCallback callback) {

        try {
            FileOutputStream fos = context.openFileOutput(attendee.getId() + ".ser", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(attendee);
            oos.close();
            fos.close();
        } catch (Exception e) {
            callback.onError(e);
        }
        Map<String, Object> updated = new HashMap<>();
        updated.put("location", attendee.getLocation());
        updated.put("checkedIn", attendee.isCheckedIn());
        updated.put("rsvp", attendee.isRsvp());

        DocumentReference userRef = database.collection("Users").document(attendee.getUser().getUsername());
        updated.put("user", userRef);

        DocumentReference eventRef = database.collection("Events").document(attendee.getEventID());
        updated.put("eventID", eventRef);

        database.collection("Attendees").document(attendee.getId())
                .update(updated)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
}
