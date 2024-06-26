package com.example.scanpal.Controllers;

import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.scanpal.Callbacks.DeleteAllAttendeesCallback;
import com.example.scanpal.Callbacks.EventDeleteCallback;
import com.example.scanpal.Callbacks.EventFetchByUserCallback;
import com.example.scanpal.Callbacks.EventFetchCallback;
import com.example.scanpal.Callbacks.EventIDsFetchCallback;
import com.example.scanpal.Callbacks.EventUpdateCallback;
import com.example.scanpal.Callbacks.EventsFetchCallback;
import com.example.scanpal.Callbacks.UserFetchCallback;
import com.example.scanpal.Models.Attendee;
import com.example.scanpal.Models.Event;
import com.example.scanpal.Models.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Handles operations related to event management in a Firestore database.
 */
public class EventController {
    private static final String TAG = "EventController";
    private final FirebaseFirestore database;
    protected ImageController imageController;
    protected AttendeeController attendeeController;
    protected QrCodeController qrCodeController;

    /**
     * Constructs an EventController with a reference to a Firestore database.
     */
    public EventController() {
        database = FirebaseFirestore.getInstance();
        imageController = new ImageController();
        attendeeController = new AttendeeController(database);
        qrCodeController = new QrCodeController();
    }

    /**
     * Constructs an EventController with a reference to a Firestore database and Firebase storage.
     * @param database The Firestore database instance.
     * @param imageController The image controller instance.
     * @param attendeeController The attendee controller instance.
     * @param qrCodeController The QR code controller instance.
     */
    public EventController(FirebaseFirestore database, ImageController imageController, AttendeeController attendeeController, QrCodeController qrCodeController) {
        this.database = database;
        this.imageController = imageController;
        this.attendeeController = attendeeController;
        this.qrCodeController = qrCodeController;
    }

    /**
     * Retrieves the Firestore database instance used for event operations.
     *
     * @return The Firestore database instance used for event operations.
     */
    public FirebaseFirestore getDatabase() {
        return this.database;
    }

    /**
     * Adds a new event to the Firestore database.
     *
     * @param event The event to be added to the database.
     * @param ID    if user chooses to reuse an existing qr code, this can contain that ID
     */
    public void addEvent(Event event, String ID) {
        Map<String, Object> eventMap = new HashMap<>();

        // have to generate ID for qr code if no ID given
        if (ID == null) {
            ID = UUID.randomUUID().toString();
        }
        event.setId(ID);
        eventMap.put("name", event.getName());
        eventMap.put("description", event.getDescription());
        eventMap.put("location", event.getLocation());
        eventMap.put("date", event.getDate());
        eventMap.put("time", event.getTime());
        eventMap.put("photo", event.getPosterURI());
        eventMap.put("capacity", event.getMaximumAttendees());
        eventMap.put("announcementCount", 0L);
        eventMap.put("trackLocation", event.isTrackLocation());
        eventMap.put("locationCoords", event.getLocationCoords());
        eventMap.put("totalCheckInCount", event.getTotalCheckInCount());

        DocumentReference organizerRef = database.collection("Users").document(event.getOrganizer().getUsername());
        eventMap.put("organizer", organizerRef);

        List<DocumentReference> participantRefs = new ArrayList<>();
        for (Attendee participant : event.getParticipants()) {
            DocumentReference participantRef = database.collection("Attendees")
                    .document(participant.getUser().getUsername());
            participantRefs.add(participantRef);
        }
        eventMap.put("participants", participantRefs);

        // Generate Qr Code or get custom code
        qrCodeController.generateAndStoreQrCode(event, eventMap);  // auto generate a qr code

        DocumentReference eventRef = database.collection("Events").document(event.getId());
        imageController.uploadImage(event.getPosterURI(), "events", event.getId() + ".jpg", uri -> {
            eventMap.put("photo", uri.toString());
            eventRef.update("photo", uri.toString());
        }, e -> Log.wtf(TAG, "Event Image Upload failed: " + e.getMessage()));
    }

    /**
     * Fetches all events from the Firestore database.
     * <p>
     * Retrieves all events stored in the Firestore collection "Events".
     * Assembles a list of Event objects representing the fetched events and utilizes a callback
     * to handle success or failure.
     *
     * @param callback Callback to manage the fetched events or errors.
     */
    public void fetchAllEvents(final EventsFetchCallback callback) {
        database.collection("Events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Event> eventsList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Manually map the document fields to the Event object
                            String id = document.getId();
                            String name = document.getString("name");
                            String description = document.getString("description");
                            String location = document.getString("location");
                            String date = document.getString("date");
                            String time = document.getString("time");
                            String photoUrlString = document.getString("photo");
                            Long maximumAttendees = document.getLong("capacity");
                            String signUpAddress = document.getString("2ndQrCode");
                            Uri photoUri = photoUrlString != null ? Uri.parse(photoUrlString) : null;
                            String infoAddress = document.getString("qrcodeurl");

                            Event event = new Event(null, name, description); // organizer is set to null temporarily
                            event.setId(id);
                            event.setLocation(location);
                            event.setDate(date);
                            event.setTime(time);
                            event.setMaximumAttendees(maximumAttendees);
                            event.setSignUpAddress(signUpAddress);
                            event.setPosterURI(photoUri);
                            event.setInfoAddress(infoAddress);
                            event.setOrganizer(null);
                            event.setParticipants(new ArrayList<>());
                            eventsList.add(event);
                        }
                        callback.onSuccess(eventsList);
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    /**
     * This will give back an arrayList of event Type, consisting of events created
     * by the current user (user is obtained from UserController.getUser())
     */
    public void getEventsByUser(View view, EventFetchByUserCallback callback) {
        ArrayList<Event> userEvents = new ArrayList<>();
        UserController userController = new UserController(view.getContext());
        userController.getUser(userController.fetchStoredUsername(), new UserFetchCallback() {
            @Override
            public void onSuccess(User user) {
                DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users")
                        .document(user.getUsername());
                database.collection("Events").whereEqualTo("organizer", userRef)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    getEventById(document.getId(), new EventFetchCallback() {
                                        @Override
                                        public void onSuccess(Event event) {
                                            userEvents.add(event);
                                            callback.onSuccess(userEvents);
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                        }
                                    });
                                }
                            }
                        });
            }

            @Override
            public void onError(Exception e) {
            }
        });
    }

    /**
     * gives an event object from database given its ID
     *
     * @param EventID  the events document ID
     * @param callback the organizer
     */
    public void getEventById(String EventID, EventFetchCallback callback) {

        database.collection("Events").document(EventID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot eventDoc = task.getResult();
                        if (eventDoc.exists()) {
                            Event event = new Event(null, Objects.requireNonNull(eventDoc.get("name")).toString(),
                                    Objects.requireNonNull(eventDoc.get("description")).toString());

                            event.setLocation(Objects.requireNonNull(eventDoc.get("location")).toString());
                            event.setDate(Objects.requireNonNull(eventDoc.get("date")).toString());
                            event.setTime(Objects.requireNonNull(eventDoc.get("time")).toString());
                            event.setMaximumAttendees((Long) Objects.requireNonNull(eventDoc.get("capacity")));
                            event.setTrackLocation(Objects.requireNonNull(eventDoc.getBoolean("trackLocation")));

                            Uri imageURI = Uri.parse(Objects.requireNonNull(eventDoc.get("photo")).toString());
                            event.setPosterURI(imageURI);
                            event.setId(EventID);
                            event.setAnnouncementCount((Long) Objects.requireNonNull(eventDoc.get("announcementCount")));

                            String locationCoords = eventDoc.getString("locationCoords");
                            if (locationCoords != null) {
                                event.setLocationCoords(locationCoords);
                            } else {
                                Log.d(TAG, "locationCoords field is missing or null for event: " + EventID);
                            }

                            fetchEventOrganizerByRef((DocumentReference) Objects.requireNonNull(eventDoc.get("organizer")),
                                    new UserFetchCallback() {
                                        @Override
                                        public void onSuccess(User user) {
                                            event.setOrganizer(user);
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                        }
                                    });
                            callback.onSuccess(event);
                        }
                    }
                });
    }

    /**
     * Edits an existing event in the Firestore database, including updating the event's image.
     *
     * @param event       The event to be updated.
     * @param newImageUri New image Uri for the event. Can be null if the image isn't being updated.
     */
    public void editEvent(Event event, @Nullable Uri newImageUri, EventUpdateCallback callback) {
        String folderPath = "events";
        String fileName = event.getId() + ".jpg";
        Map<String, Object> eventMap = new HashMap<>();
        Runnable updateEventDetails = () -> {
            eventMap.put("name", event.getName());
            eventMap.put("description", event.getDescription());
            eventMap.put("location", event.getLocation());
            eventMap.put("locationCoords", event.getLocationCoords());
            eventMap.put("date", event.getDate());
            eventMap.put("time", event.getTime());
            eventMap.put("capacity", event.getMaximumAttendees());
            eventMap.put("announcementCount", event.getAnnouncementCount());
            eventMap.put("trackLocation", event.isTrackLocation());
            DocumentReference eventRef = database.collection("Events").document(event.getId());
            eventRef.update(eventMap).addOnSuccessListener(aVoid -> callback.onSuccess(true)).addOnFailureListener(callback::onError);
        };
        if (newImageUri != null) {
            imageController.uploadImage(newImageUri, folderPath, fileName, uri -> {
                eventMap.put("photo", uri.toString());
                updateEventDetails.run();
            }, callback::onError);
        } else {
            eventMap.put("photo", event.getPosterURI());
            updateEventDetails.run();
            callback.onSuccess(true);
        }
    }

    /**
     * Fetches the organizer of an event referenced by a DocumentReference from the Firestore database.
     * Retrieves the organizer details from Firestore using the provided DocumentReference.
     * Assembles a User object representing the organizer and utilizes a callback
     * to handle success or failure.
     *
     * @param eventRef The DocumentReference pointing to the event organizer's document.
     * @param callback Callback to manage the fetched organizer or errors.
     */
    public void fetchEventOrganizerByRef(DocumentReference eventRef, UserFetchCallback callback) {
        if (eventRef == null) {
            // Early return or callback error if the DocumentReference is null
            Log.e(TAG, "Event Reference is null.");
            callback.onError(new NullPointerException("Event Reference is null."));
            return;
        }
        eventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot organizerDoc = task.getResult();
                if (organizerDoc.exists()) {
                    // Assuming all the fields are correctly spelled and present in the document
                    String firstName = organizerDoc.getString("firstName");
                    String lastName = organizerDoc.getString("lastName");
                    String homepage = organizerDoc.getString("homepage");
                    String deviceToken = organizerDoc.getString("deviceToken");
                    Boolean administrator = organizerDoc.getBoolean("administrator");
                    String photo = organizerDoc.getString("photo");

                    if (firstName != null && lastName != null && deviceToken != null && administrator != null && photo != null) {
                        User organizer = new User(organizerDoc.getId(), firstName, lastName, photo, homepage, deviceToken);
                        organizer.setAdministrator(administrator);
                        callback.onSuccess(organizer);
                    } else {
                        // Handle the case where one of the fields is null
                        Log.e(TAG, "One of the user fields is missing or null.");
                        callback.onError(new IllegalArgumentException("One of the user fields is missing or null."));
                    }
                } else {
                    // Handle the case where the organizer document doesn't exist
                    Log.e(TAG, "Organizer document does not exist.");
                    callback.onError(new IllegalArgumentException("Organizer document does not exist."));
                }
            } else {
                // Handle the case where the task was not successful
                Log.e(TAG, "Task failed to fetch organizer document.");
                callback.onError(task.getException());
            }
        });
    }


    /**
     * Retrieves all event IDs from the Firestore database.
     * Retrieves the IDs of all events stored in the Firestore collection "Events".
     * Assembles a list of event IDs and utilizes a callback to handle success or failure.
     *
     * @param callback Callback to manage the fetched event IDs or errors.
     */

    public void getAllEventIds(EventIDsFetchCallback callback) {
        CollectionReference eventsRef = database.collection("Events");
        List<String> documentIds = new ArrayList<>();

        eventsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    String documentId = document.getId();
                    documentIds.add(documentId);
                }
                callback.onSuccess(documentIds);
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public void deleteEvent(String eventID, EventDeleteCallback callback) {
        DocumentReference eventRef = database.collection("Events").document(eventID);
        attendeeController.deleteAllAttendeesForEvent(eventID, new DeleteAllAttendeesCallback() {
            @Override
            public void onSuccess() {
                imageController.deleteImage("events", eventID + ".jpg");
                imageController.deleteImage("qr-codes", eventID + "-check-in.png");
                imageController.deleteImage("qr-codes", eventID + "-event.png");
                eventRef.delete()
                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                        .addOnFailureListener(callback::onError);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * Deletes all events organized by the specified user from the Firestore database.
     *
     * @param username The username of the user whose events are to be deleted.
     * @param callback The callback to handle the result of the deletion process.
     */
    public void deleteEventsByOrganizer(String username, EventDeleteCallback callback) {
        DocumentReference userRef = database.collection("Users").document(username);
        database.collection("Events").whereEqualTo("organizer", userRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String eventID = document.getId();
                            deleteEvent(eventID, new EventDeleteCallback() {
                                @Override
                                public void onSuccess() {
                                    Log.wtf(TAG, "Event deleted successfully: " + eventID);
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.wtf(TAG, "Failed to delete event: " + eventID, e);
                                }
                            });
                        }
                        callback.onSuccess();
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }
}