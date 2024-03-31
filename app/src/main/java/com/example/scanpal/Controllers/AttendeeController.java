package com.example.scanpal.Controllers;

import android.util.Log;

import com.example.scanpal.Callbacks.AttendeeAddCallback;
import com.example.scanpal.Callbacks.AttendeeDeleteCallback;
import com.example.scanpal.Callbacks.AttendeeFetchCallback;
import com.example.scanpal.Callbacks.AttendeeSignedUpFetchCallback;
import com.example.scanpal.Callbacks.AttendeeUpdateCallback;
import com.example.scanpal.Callbacks.DeleteAllAttendeesCallback;
import com.example.scanpal.Models.Attendee;
import com.example.scanpal.Models.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages attendee data interactions with a Firestore database and internal storage.
 * Provides methods to add, fetch, update, and delete attendee records.
 */
public class AttendeeController {
    private final FirebaseFirestore database;

    /**
     * Instantiates a controller for managing attendee data.
     *
     * @param database The Firestore database instance for data operations.
     */
    public AttendeeController(FirebaseFirestore database) {
        this.database = database;
    }

    /**
     * Retrieves the Firestore database instance associated with this controller.
     *
     * @return The Firestore database instance.
     */
    public FirebaseFirestore getDatabase() {
        return this.database;
    }

    /**
     * Adds an attendee to Firestore.
     * <p>
     * Serializes the {@link Attendee} object for local storage and creates a map
     * of attendee properties for Firestore. Handles the success or error of the addition process.
     *
     * @param attendee The attendee object to add.
     * @param callback Callback to handle the outcome of the addition process.
     */
    public void addAttendee(Attendee attendee, AttendeeAddCallback callback) {

        Map<String, Object> attendeeMap = new HashMap<>();
        attendeeMap.put("location", attendee.getLocation());
        attendeeMap.put("checkedIn", attendee.isCheckedIn());
        attendeeMap.put("rsvp", attendee.isRsvp());
        attendeeMap.put("checkInCount", attendee.getCheckinCount());

        DocumentReference userRef = database.collection("Users").document(attendee.getUser().getUsername());
        attendeeMap.put("user", userRef);

        DocumentReference eventRef = database.collection("Events").document(attendee.getEventID());
        attendeeMap.put("eventID", eventRef);

        database.collection("Attendees").document(attendee.getId()).set(attendeeMap)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Fetches the details of an attendee by their ID.
     * <p>
     * Retrieve the attendee from Firestore. Utilizes callback to handle the result or any errors.
     *
     * @param attendeeId The unique ID of the attendee to fetch.
     * @param callback   Callback to handle the fetched attendee or errors.
     */
    public void fetchAttendee(String attendeeId, AttendeeFetchCallback callback) {

        database.collection("Attendees").document(attendeeId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Attendee attendee = new Attendee();
                        attendee.setId(attendeeId);
                        attendee.setLocation(documentSnapshot.getString("location"));
                        attendee.setCheckedIn(Boolean.TRUE.equals(documentSnapshot.getBoolean("checkedIn")));
                        attendee.setRsvp(Boolean.TRUE.equals(documentSnapshot.getBoolean("rsvp")));
                        attendee.setCheckinCount((long)documentSnapshot.get("checkInCount"));

                        //Log.wtf("FETCHONG ATTENDEE", "crash here:"  + documentSnapshot.getBoolean("rsvp").toString());


                        DocumentReference userRef = documentSnapshot.getDocumentReference("user");
                        DocumentReference eventRef = documentSnapshot.getDocumentReference("eventID");

                        if (userRef != null && eventRef != null) {
                            userRef.get().addOnSuccessListener(userDoc -> {
                                User user = userDoc.toObject(User.class);
                                user.setUsername( userRef.getId() );

                                Log.wtf("FETCHONG ATTENDEE", "crash here get user: "  + user.getUsername());

                                attendee.setUser(user);
                                attendee.setEventID(eventRef.getId());
                                callback.onSuccess(attendee);
                            }).addOnFailureListener(callback::onError);


                            Log.wtf("FETCHONG ATTENDEE", "crash here: ansyc" );


                        } else {
                            callback.onError(new Exception("User reference or Event reference not found in attendee document"));
                        }
                    } else {
                        callback.onError(new Exception("Attendee document not found"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }


    /**
     * Updates the details of an existing attendee in Firestore.
     * <p>
     * Updates the attendee's details in Firestore. Uses callback to handle success or errors.
     *
     * @param attendee The updated attendee object.
     * @param callback Callback to handle the outcome of the update process.
     */
    public void updateAttendee(Attendee attendee, AttendeeUpdateCallback callback) {

        Map<String, Object> updated = new HashMap<>();
        updated.put("location", attendee.getLocation());
        updated.put("checkedIn", attendee.isCheckedIn());
        updated.put("rsvp", attendee.isRsvp());
        updated.put("checkInCount", attendee.getCheckinCount());
        if( attendee.getUser() == null) {
            Log.wtf("CHECKED IN!", "crash here loc:"  + attendee.getLocation());
        }




        Log.wtf("CHECKED IN!", "path: User/"  + attendee.getUser().getUsername());

        DocumentReference userRef = database.collection("Users").document(attendee.getUser().getUsername());
        updated.put("user", userRef);

        DocumentReference eventRef = database.collection("Events").document(attendee.getEventID());
        updated.put("eventID", eventRef);

        database.collection("Attendees").document(attendee.getId())
                .update(updated)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Deletes an attendee's record from Firestore by their unique ID.
     * <p>
     * Deletes the attendee record from Firestore. Uses callback to manage success or error outcomes.
     *
     * @param attendeeId The unique ID of the attendee to delete.
     * @param callback   Callback to handle the outcome of the deletion process.
     */
    public void deleteAttendee(String attendeeId, final AttendeeDeleteCallback callback) {
        database.collection("Attendees").document(attendeeId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Fetches all attendees who have RSVP'd for a given event.
     * <p>
     * Retrieves attendees from Firestore where the RSVP is true for the specified eventID.
     * Assembles a list of {@link Attendee} objects for successful RSVPs and utilizes a callback
     * to handle success or failure.
     *
     * @param eventID  The unique ID of the event.
     * @param callback Callback to manage the fetched attendees or errors.
     */
    public void fetchSignedUpUsers(String eventID, AttendeeSignedUpFetchCallback callback) {
        DocumentReference eventRef = database.collection("Events").document(eventID);
        ArrayList<Attendee> attendees = new ArrayList<>();

        database.collection("Attendees")
                .whereEqualTo("eventID", eventRef)
                .whereEqualTo("rsvp", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onSuccess(attendees);
                        return;
                    }

                    AtomicInteger completedFetches = new AtomicInteger();
                    int totalAttendees = queryDocumentSnapshots.size();

                    queryDocumentSnapshots.forEach(attendeeDoc -> {
                        DocumentReference userRef = attendeeDoc.getDocumentReference("user");
                        if (userRef == null) {
                            if (completedFetches.incrementAndGet() == totalAttendees) {
                                callback.onSuccess(attendees);
                            }
                            return;
                        }
                        userRef.get().addOnSuccessListener(userDocSnapshot -> {
                            if (userDocSnapshot.exists()) {
                                User user = new User(
                                        userDocSnapshot.getId(),
                                        userDocSnapshot.getString("firstName"),
                                        userDocSnapshot.getString("lastName"),
                                        userDocSnapshot.getString("photo"),
                                        userDocSnapshot.getString("homepage"),
                                        userDocSnapshot.getString("deviceToken")
                                );

                                Attendee attendee = new Attendee(
                                        user,
                                        Objects.requireNonNull(attendeeDoc.getDocumentReference("eventID")).toString(),
                                        Boolean.TRUE.equals(attendeeDoc.getBoolean("rsvp")),
                                        Boolean.TRUE.equals(attendeeDoc.getBoolean("checkedIn")),
                                        (long)attendeeDoc.get("checkInCount")
                                );
                                attendee.setLocation(attendeeDoc.getString("location"));
                                attendees.add(attendee);
                            }
                            if (completedFetches.incrementAndGet() == totalAttendees) {
                                callback.onSuccess(attendees);
                            }
                        }).addOnFailureListener(e -> callback.onFailure(new Exception("Error fetching attendee user details", e)));
                    });
                })
                .addOnFailureListener(e -> callback.onFailure(new Exception("Error fetching attendees", e)));
    }

    public void fetchCheckedInUsers(String eventID, AttendeeSignedUpFetchCallback callback) {
        DocumentReference eventRef = database.collection("Events").document(eventID);
        ArrayList<Attendee> attendees = new ArrayList<>();

        database.collection("Attendees")
                .whereEqualTo("eventID", eventRef)
                .whereEqualTo("checkedIn", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onSuccess(attendees);
                        return;
                    }

                    AtomicInteger completedFetches = new AtomicInteger();
                    int totalAttendees = queryDocumentSnapshots.size();

                    queryDocumentSnapshots.forEach(attendeeDoc -> {
                        DocumentReference userRef = attendeeDoc.getDocumentReference("user");
                        if (userRef == null) {
                            if (completedFetches.incrementAndGet() == totalAttendees) {
                                callback.onSuccess(attendees);
                            }
                            return;
                        }
                        userRef.get().addOnSuccessListener(userDocSnapshot -> {
                            if (userDocSnapshot.exists()) {
                                User user = new User(
                                        userDocSnapshot.getId(),
                                        userDocSnapshot.getString("firstName"),
                                        userDocSnapshot.getString("lastName"),
                                        userDocSnapshot.getString("photo"),
                                        userDocSnapshot.getString("homepage"),
                                        userDocSnapshot.getString("deviceToken")
                                );

                                Attendee attendee = new Attendee(
                                        user,
                                        Objects.requireNonNull(attendeeDoc.getDocumentReference("eventID")).toString(),
                                        Boolean.TRUE.equals(attendeeDoc.getBoolean("rsvp")),
                                        Boolean.TRUE.equals(attendeeDoc.getBoolean("checkedIn")),
                                        (long) attendeeDoc.get("checkInCount")
                                );
                                attendee.setLocation(attendeeDoc.getString("location"));
                                attendees.add(attendee);
                            }
                            if (completedFetches.incrementAndGet() == totalAttendees) {
                                callback.onSuccess(attendees);
                            }
                        }).addOnFailureListener(e -> callback.onFailure(new Exception("Error fetching attendee user details", e)));
                    });
                })
                .addOnFailureListener(e -> callback.onFailure(new Exception("Error fetching attendees", e)));
    }

    /**
     * Deletes all attendee records associated with a specific user from Firestore and local storage.
     * <p>
     * Queries Firestore for all attendees linked to the specified username, then deletes
     * each record and its corresponding local storage file. Uses callback to handle success or errors.
     *
     * @param username The username associated with the attendee records to delete.
     * @param callback Callback to handle the outcome of the deletion process.
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
                    }
                    batch.commit().addOnSuccessListener(aVoid -> callback.onSuccess()).addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Deletes all attendees for a specific event from Firestore.
     *
     * @param eventID  The unique ID of the event.
     * @param callback Callback to handle the outcome of the deletion process.
     */
    public void deleteAllAttendeesForEvent(String eventID, final DeleteAllAttendeesCallback callback) {
        DocumentReference eventRef = database.collection("Events").document(eventID);

        database.collection("Attendees")
                .whereEqualTo("eventID", eventRef)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = database.batch();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DocumentReference attendeeRef = document.getReference();
                        batch.delete(attendeeRef);
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }
}

