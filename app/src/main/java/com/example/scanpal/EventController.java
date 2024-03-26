package com.example.scanpal;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
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

    private static final String TAG = "EventController";
    private final FirebaseFirestore database;
    private final FirebaseStorage storage;
    protected ImageController imageController;

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
     * @param ID if user chooses to reuse an existing qr code, this can contain that ID
     */
    public void addEvent(Event event, String ID) {
        Map<String, Object> eventMap = new HashMap<>();
        Boolean isCustom = Boolean.TRUE;  // to keep track of whether custom qr code or not

        // have to generate ID for qr code if no ID given
        if (ID == null) {
            UUID uuid = UUID.randomUUID();
            ID = uuid.toString();
            isCustom = Boolean.FALSE;
        } else {
            ID = ID.substring(8);  // delete the https part to ensure the link can be saved
        }
        event.setId(ID);
        eventMap.put("name", event.getName());
        eventMap.put("description", event.getDescription());
        eventMap.put("location", event.getLocation());
        eventMap.put("photo", event.getPosterURI());
        eventMap.put("capacity", event.getMaximumAttendees());
        eventMap.put("announcementCount", 0L);//event.getAnnouncementCount());// Initialize to 0

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
        QrCodeController qrCodeController = new QrCodeController();
        qrCodeController.generateAndStoreQrCode(event, eventMap, isCustom);  // auto generate a qr code

        StorageReference storageRef = storage.getReference();
        DocumentReference eventRef = database.collection("Events").document(event.getId());
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
                            event.setAnnouncementCount((long) eventDoc.get("announcementCount"));

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
                    String deviceToken = organizerDoc.getString("deviceToken");
                    Boolean administrator = organizerDoc.getBoolean("administrator");
                    String photo = organizerDoc.getString("photo");

                    if (firstName != null && lastName != null && deviceToken != null && administrator != null && photo != null) {
                        User organizer = new User(organizerDoc.getId(), firstName, lastName, deviceToken);
                        organizer.setAdministrator(administrator);
                        organizer.setPhoto(photo);
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

                for (String id : documentIds) {
                    Log.d("Document ID", id);
                }
            } else {
                Log.d("Firestore", "Error getting documents: ", task.getException());
            }
        });

    }

    public void deleteEvent(String eventID, EventDeleteCallback callback) {
        imageController.deleteImage("events", "event_" + eventID + ".jpg");
        imageController.deleteImage("qr-codes", eventID + "-check-in.png");
        imageController.deleteImage("qr-codes", eventID + "-event.png");
        DocumentReference eventRef = database.collection("Events").document(eventID);
        eventRef.delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
}