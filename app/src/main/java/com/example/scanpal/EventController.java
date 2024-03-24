package com.example.scanpal;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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

    protected FirebaseFirestore database;
    protected FirebaseStorage storage;
    protected ImageController imageController;

    /**
     * Constructs an EventController with a reference to a Firestore database.
     */
    public EventController() {
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        imageController = new ImageController();
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
     */
    public void addEvent(Event event) {
        Map<String, Object> eventMap = new HashMap<>();
        String uuid = UUID.randomUUID().toString();
        event.setId(uuid);

        eventMap.put("name", event.getName());
        eventMap.put("description", event.getDescription());
        eventMap.put("location", event.getLocation());
        eventMap.put("photo", event.getPosterURI());
        eventMap.put("capacity", event.getMaximumAttendees());
        eventMap.put("announcementCount", 0L);

        DocumentReference organizerRef = database.collection("Users").document(event.getOrganizer().getUsername());
        eventMap.put("organizer", organizerRef);

        List<DocumentReference> participantRefs = new ArrayList<>();
        for (Attendee participant : event.getParticipants()) {
            DocumentReference participantRef = database.collection("Attendees")
                    .document(participant.getUser().getUsername());
            participantRefs.add(participantRef);
        }
        eventMap.put("participants", participantRefs);

        // Creating bitmap for qrcode and add it to event
        Bitmap qr_to_event = QrCodeController.generate("E" + event.getId());
        event.setQrToEvent(qr_to_event);

        // Storing the bitmap into firebase by converting into byte array
        assert qr_to_event != null;
        byte[] imageDataEvent = QrCodeController.bitmapToByteArray(qr_to_event);

        // Creating bitmap for qrcode check-in
        Bitmap qr_to_checkin = QrCodeController.generate("C" + event.getId());
        event.setQrToCheckIn(qr_to_checkin);

        // Storing the bitmap into firebase by converting into byte array
        assert qr_to_checkin != null;
        byte[] imageDataCheckin = QrCodeController.bitmapToByteArray(qr_to_checkin);

        // upload qr code images
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/png")
                .build();
        StorageReference storageRef = storage.getReference();
        DocumentReference eventRef = database.collection("Events").document(event.getId());

        // Save to database
        eventRef.set(eventMap)
                .addOnSuccessListener(aVoid -> {
                    StorageReference checkInQRCodeRef = storageRef.child("qr-codes/" + event.getId() + "-check-in.png");
                    StorageReference eventQRCodeRef = storageRef.child("qr-codes/" + event.getId() + "-event.png");
                    UploadTask uploadTask = checkInQRCodeRef.putBytes(imageDataCheckin, metadata);
                    uploadTask
                            .addOnFailureListener(exception -> Log.e("FirebaseStorage",
                                    "Failed to upload check in qr code: " + exception.getMessage()))
                            .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                                eventMap.put("checkInQRCodeUrl", uri.toString());
                                // Save to database
                                eventRef.update("checkInQRCodeURL", uri);
                            }).addOnFailureListener(exception -> Log.e("FirebaseStorage",
                                    "Failed to get download url for check in qr code" + exception.getMessage())));
                    uploadTask = eventQRCodeRef.putBytes(imageDataEvent, metadata);
                    uploadTask
                            .addOnFailureListener(exception -> Log.e("FirebaseStorage",
                                    "Failed to upload event qr code: " + exception.getMessage()))

                            .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                                eventMap.put("eventQRCodeUrl", uri.toString());
                                // Save to database
                                eventRef.update("eventQRCodeURL", uri);
                            }).addOnFailureListener(exception -> Log.e("FirebaseStorage",
                                    "Failed to get download url for event qr code" + exception.getMessage())));
                })
                .addOnFailureListener(e -> Log.d("EventController", "Error adding event: " + e.getMessage()));

        StorageReference eventPosterRef = storageRef.child("events/" + "event_" + event.getId() + ".jpg");
        eventPosterRef.putFile(event.getPosterURI());
        UploadTask uploadPhotoTask;

        uploadPhotoTask = eventPosterRef.putFile(event.getPosterURI());

        uploadPhotoTask.addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
            eventMap.put("photo", uri.toString());
            eventRef.update("photo", uri.toString())
                    .addOnSuccessListener(aVoid -> Log.d("EventController", "Photo URL added successfully!"))
                    .addOnFailureListener(exception -> Log.e("EventController", "Failed to update photo URL: " + exception.getMessage()));
        }).addOnFailureListener(exception -> Log.e("FirebaseStorage", "Failed to get download URL for event photo: " + exception.getMessage()))).addOnFailureListener(exception -> Log.e("FirebaseStorage", "Failed to upload event photo: " + exception.getMessage()));
    }

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
                            String photoUrlString = document.getString("photo");
                            long maximumAttendees = document.getLong("capacity");
                            String signUpAddress = document.getString("2ndQrCode");
                            Uri photoUri = photoUrlString != null ? Uri.parse(photoUrlString) : null;
                            String infoAddress = document.getString("qrcodeurl");

                            Event event = new Event(null, name, description); // organizer is set to null temporarily
                            event.setId(id);
                            event.setLocation(location);
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
                        if (task.getException() != null) {
                            callback.onError(task.getException());
                        }
                    }
                });
    }


    /**
     * This will give back an arrayList of event Type, consisting of events created
     * by the current user (user is obtained from UserController.getUser())
     */
    public void getEventsByUser(View view, EventFetchByUserCallback callback) {
        // TODO: a clause for admins to return all existing events

        ArrayList<Event> userEvents = new ArrayList<>();
        UserController userController = new UserController(FirebaseFirestore.getInstance(), view.getContext());

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
                            event.setMaximumAttendees((long) eventDoc.get("capacity"));

                            Uri imageURI = Uri.parse(Objects.requireNonNull(eventDoc.get("photo")).toString());
                            event.setPosterURI(imageURI);
                            event.setId(EventID);
                            event.setAnnouncementCount((long) eventDoc.get("announcementCount"));

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
        String fileName = "event_" + event.getId() + ".jpg";
        Map<String, Object> eventMap = new HashMap<>();

        Runnable updateEventDetails = () -> {
            eventMap.put("name", event.getName());
            eventMap.put("description", event.getDescription());
            eventMap.put("location", event.getLocation());
            eventMap.put("capacity", event.getMaximumAttendees());
            eventMap.put("announcementCount", event.getAnnouncementCount());

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

    public void fetchEventOrganizerByRef(DocumentReference eventRef, UserFetchCallback callback) {

        eventRef.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                DocumentSnapshot organizerDoc = task.getResult();

                if (organizerDoc.exists()) {
                    User organizer = new User(organizerDoc.getId(),
                            Objects.requireNonNull(organizerDoc.get("firstName")).toString(), Objects.requireNonNull(organizerDoc.get("lastName")).toString(),
                            Objects.requireNonNull(organizerDoc.get("deviceToken")).toString());
                    organizer.setAdministrator((boolean) organizerDoc.get("administrator"));
                    organizer.setPhoto(Objects.requireNonNull(organizerDoc.get("photo")).toString());
                    callback.onSuccess(organizer);
                }
            }
        });
    }

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
            }
        });
    }
}