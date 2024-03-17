package com.example.scanpal;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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

    private final FirebaseFirestore database;
    private final FirebaseStorage storage;

    /**
     * Constructs an EventController with a reference to a Firestore database.
     */
    public EventController() {
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
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
        // have to generate for qr code
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();

        event.setId(uuidString);

        eventMap.put("name", event.getName());
        eventMap.put("description", event.getDescription());
        eventMap.put("location", event.getLocation());
        eventMap.put("photo", event.getPosterURI());
        eventMap.put("capacity", event.getMaximumAttendees());
        eventMap.put("announcementCount", event.getAnnouncementCount());

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
                    Log.d("EventController", "Event added successfully!");
                    StorageReference checkInQRCodeRef = storageRef.child("qr-codes/" + event.getId() + "-check-in.png");
                    StorageReference eventQRCodeRef = storageRef.child("qr-codes/" + event.getId() + "-event.png");
                    // StorageReference eventPosterRef = storageRef.child("/" + event.getId() +
                    // "-poster.jpg");//TODO: checking img types?
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

        StorageReference eventPosterRef = storageRef.child("/" + event.getId() + "-poster.jpg");
        // TODO: check types?
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

                            // init new event object
                            Event event = new Event(null, name, description); // organizer is set to null temporarily
                            event.setId(id);
                            event.setLocation(location);
                            event.setMaximumAttendees(maximumAttendees);
                            event.setSignUpAddress(signUpAddress);
                            event.setPosterURI(photoUri);
                            event.setInfoAddress(infoAddress);

                            event.setOrganizer(null);
                            event.setParticipants(new ArrayList<>());

                            // Add the event to the list
                            eventsList.add(event);
                        }

                        callback.onSuccess(eventsList);
                    } else {
                        if (task.getException() != null) {
                            Log.e("EventController", "Error getting documents: ", task.getException());
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
                Log.d("LISTENER", "IN COMPLETE LISTENER");
                Log.d("USERNAME", "/Users/" + user.getUsername());

                DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users")
                        .document(user.getUsername());

                database.collection("Events").whereEqualTo("organizer", userRef)
                        .get()
                        .addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // loop through all events to get events that match
                                    Log.d("E ORGANIZER", Objects.requireNonNull(document.get("name")).toString());

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
                            } else {
                                Log.d("EVENTTRACKING", "NOT SUCCESSFUL");
                                // resolve errors
                            }
                        });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(view.getContext(), "Failed to fetch User Data", Toast.LENGTH_LONG).show();
                Log.d("USERERROR", "NO FETCH");
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
                    Log.d("GETBYEID", "IN ON COMPLETE");
                    if (task.isSuccessful()) {
                        DocumentSnapshot eventDoc = task.getResult();
                        if (eventDoc.exists()) {
                            // Document exists, retrieve its data
                            Log.d("GETBYEID", "Document found with ID: " + EventID);

                            // null user is set a few lines later
                            Event event = new Event(null, Objects.requireNonNull(eventDoc.get("name")).toString(),
                                    Objects.requireNonNull(eventDoc.get("description")).toString());

                            event.setLocation(Objects.requireNonNull(eventDoc.get("location")).toString());
                            // event.setInfoAddress( eventDoc.get("eventQRCodeURL").toString() );//assuming
                            // address to the info?
                            // event.setSignUpAddress( eventDoc.get("checkInQRCodeURL").toString()
                            // );//address to qrcheck in?
                            event.setMaximumAttendees((long) eventDoc.get("capacity"));

                            // if event doesn't have an image this will cause a crash
                            Uri imageURI = Uri.parse(Objects.requireNonNull(eventDoc.get("photo")).toString());
                            event.setPosterURI(imageURI);
                            event.setId(EventID);
                            event.setAnnouncementCount( (long)eventDoc.get("announcementCount") );

                            // Access the data using eventDoc.getData() or convert it to an object
                            fetchEventOrganizerByRef((DocumentReference) Objects.requireNonNull(eventDoc.get("organizer")),
                                    new UserFetchCallback() {
                                        @Override
                                        public void onSuccess(User user) {
                                            event.setOrganizer(user);
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Log.d("FetchingEventbyID", "Error getting Event");
                                        }
                                    });

                            callback.onSuccess(event);
                        } else {
                            // Document does not exist
                            Log.d("GETBYEID", "No document found with ID: " + EventID);
                        }
                    } else {
                        // Error occurred while fetching document
                        Log.d("GETBYEID", "Error getting document with ID: " + EventID, task.getException());
                    }
                });
    }

    /**
     * edits an existing event in database given its ID
     *
     * @param EventID the event to edit
     * @param event   the new details for the existing event
     */
    public void editEventById(String EventID, Event event) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", event.getName());
        updates.put("location", event.getLocation());
        updates.put("description", event.getDescription());
        updates.put("capacity", event.getMaximumAttendees());
        updates.put("announcementCount", event.getAnnouncementCount()); //TODO: TEST FOR BUGS LATER

        Map<String, Object> eventMap = new HashMap<>();

        StorageReference storageRef = storage.getReference();
        DocumentReference eventRef = database.collection("Events").document(EventID);

        StorageReference eventPosterRef = storageRef.child("/" + EventID + "-poster.jpg");// TODO: checking img types?
        eventPosterRef.putFile(event.getPosterURI());
        UploadTask uploadPhotoTask;

        uploadPhotoTask = eventPosterRef.putFile(event.getPosterURI());

        uploadPhotoTask.addOnSuccessListener(taskSnapshot -> {
            // Photo is uploaded to the storage; get download URL
            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                // Update eventMap with the photo URL
                eventMap.put("photo", uri.toString());

                // Update the event document in Firestore with the photo URL
                eventRef.update("photo", uri.toString())
                        .addOnSuccessListener(aVoid -> Log.d("EventController", "Photo URL added successfully!"))
                        .addOnFailureListener(exception -> Log.e("EventController", "Failed to update photo URL: " + exception.getMessage()));
            }).addOnFailureListener(exception -> Log.e("FirebaseStorage", "Failed to get download URL for event photo: " + exception.getMessage()));
        }).addOnFailureListener(exception -> Log.e("FirebaseStorage", "Failed to upload event photo: " + exception.getMessage()));

        database.collection("Events").document(EventID)
                .update(updates)
                .addOnSuccessListener(unused -> Log.d("DB", "Document successfully updated!")).addOnFailureListener(e -> Log.w("DB ERROR", "Error updating document", e));
    }

    public void fetchEventOrganizerByRef(DocumentReference eventRef, UserFetchCallback callback) {

        eventRef.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                DocumentSnapshot organizerDoc = task.getResult();

                if (organizerDoc.exists()) {
                    User organizer = new User(organizerDoc.getId(),
                            Objects.requireNonNull(organizerDoc.get("firstName")).toString(), Objects.requireNonNull(organizerDoc.get("lastName")).toString());
                    organizer.setAdministrator((boolean) organizerDoc.get("administrator"));
                    organizer.setPhoto(Objects.requireNonNull(organizerDoc.get("photo")).toString());
                    callback.onSuccess(organizer);
                }
            }
        });
    }
}