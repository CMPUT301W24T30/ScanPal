package com.example.scanpal;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages operations related to attendee management within a Firestore database. This class
 * provides functionality to add, fetch, and update attendee records in Firestore, facilitating
 * the tracking and management of event participants. It leverages Firebase Firestore and
 * device Internal Storage for persistent storage and retrieval of attendee data.
 */
public class AttendeeController {
    private final FirebaseFirestore database;
    private final Context context;

    /**
     * Constructs an AttendeeController with a specified Firestore database instance and
     * application context. This constructor initializes the controller ready for attendee
     * data management operations.
     *
     * @param database The Firestore database instance for operations.
     * @param context  The application context used for file operations.
     */
    public AttendeeController(FirebaseFirestore database, Context context) {
        this.database = database;
        this.context = context;
    }

    public FirebaseFirestore getDatabase() {
        return this.database;
    }

    /**
     * Adds a new attendee record to the Firestore database and optionally to local storage.
     * This method serializes the {@link Attendee} object and saves it, then updates the Firestore
     * database with attendee details including location, RSVP status, and event association.
     *
     * @param attendee The attendee to be added to the database.
     * @param callback An {@link AttendeeAddCallback} to handle success or error outcomes.
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

        Map<String, Object> attendeeMap = new HashMap<>();
        attendeeMap.put("location", attendee.getLocation());
        attendeeMap.put("checkedIn", attendee.isCheckedIn());
        attendeeMap.put("rsvp", attendee.isRsvp());

        DocumentReference userRef = database.collection("Users").document(attendee.getUser().getUsername());
        attendeeMap.put("user", userRef);

        DocumentReference eventRef = database.collection("Events").document(attendee.getEventID());
        attendeeMap.put("eventID", eventRef);

        database.collection("Attendees").document(attendee.getId()).set(attendeeMap)
                .addOnSuccessListener(aVoid -> System.out.println("Attendee added successfully!"))
                .addOnFailureListener(e -> System.out.println("Error adding attendee: " + e.getMessage()));
    }

    /**
     * Fetches an attendee's details from the Firestore database or local storage if available.
     * This method attempts to retrieve the attendee details based on the attendee ID, providing
     * an async callback with the result.
     *
     * @param attendeeId The unique ID of the attendee to fetch.
     * @param callback   An {@link AttendeeFetchCallback} to handle the fetched data or errors.
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
            callback.onError(e);
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
     * Updates an existing attendee's information in the Firestore database and local storage.
     * This method allows updating fields such as RSVP status and checked-in status, ensuring
     * the attendee record is current.
     *
     * @param attendee The {@link Attendee} object with updated details to be saved.
     * @param callback An {@link AttendeeUpdateCallback} to handle success or error outcomes.
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

    interface OnCompleteSignedUpAttendees {
        void onSuccess(ArrayList<Attendee> attendees);
        void onFailure(Exception e);
    }

    /**
     * Given an event id, fetch all signed up attendees for the corresponding event.
     * Signed up user means its rsvp field is set to true.
     * @param eventID String event id
     * @param listener OnCompleteSignedUpAttendees
     */
    public void fetchSignedUpUsers(String eventID, OnCompleteSignedUpAttendees listener) {
        DocumentReference eventRef = database.collection("Events").document(eventID);
        ArrayList<Attendee> attendees = new ArrayList<>();

        AtomicInteger attendeesCounter = new AtomicInteger();

        database.collection("Attendees")
                .whereEqualTo("eventID", eventRef)
                .whereEqualTo("rsvp", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalAttendees = queryDocumentSnapshots.size();

                    if (totalAttendees == 0) {
                        return;
                    }

                    for (QueryDocumentSnapshot attendeeDoc : queryDocumentSnapshots) {
                        String eventID_remote = Objects.requireNonNull(attendeeDoc.getDocumentReference("eventID")).toString();
                        boolean rsvp = Boolean.TRUE.equals(attendeeDoc.getBoolean("rsvp"));
                        boolean checkedIn = Boolean.TRUE.equals(attendeeDoc.getBoolean("checkedIn"));
                        String location = attendeeDoc.getString("location");

                        DocumentReference userRef = attendeeDoc.getDocumentReference("user");
                        userRef.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot userDoc = task.getResult();

                                if (userDoc != null && userDoc.exists()) {
                                    int completedFetches = attendeesCounter.incrementAndGet();

                                    User user = new User(
                                            userDoc.getId(),
                                            userDoc.getString("firstName"),
                                            userDoc.getString("lastName"),
                                            userDoc.getString("photo")
                                    );

                                    Attendee attendee = new Attendee(user, eventID_remote, rsvp, checkedIn);
                                    attendee.setLocation(location);
                                    attendees.add(attendee);
                                    Log.d("AttendeeController", user.getUsername());
                                    Log.d("AttendeeController", user.getFirstName());
                                    Log.d("AttendeeController", user.getLastName());
                                    Log.d("AttendeeController", user.getPhoto());



                                    if (completedFetches == totalAttendees) {
                                        listener.onSuccess(attendees);
                                    }
                                } else {
                                    listener.onFailure(new Exception("User does not exist"));
                                }


                            } else {
                                listener.onFailure(new Exception("Error fetching user", task.getException()));
                            }
                        });

                    }

                })
                .addOnFailureListener(e -> listener.onFailure(e));

    }

    /**
     * Deletes all attendee records associated with a user from Firestore and local storage.
     *
     * @param username The username of the user whose attendee records are to be deleted.
     * @param callback The callback for success or error handling.
     */
    public void deleteAllUserAttendees(String username, final DeleteAllAttendeesCallback callback) {
        database.collection("Attendees")
                .whereEqualTo("user", database.collection("Users").document(username))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = database.batch();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DocumentReference attendeeRef = database.collection("Attendees").document(document.getId());
                        batch.delete(attendeeRef);
                        context.deleteFile(document.getId() + ".ser");
                    }
                    batch.commit().addOnSuccessListener(aVoid -> callback.onSuccess()).addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }
}
