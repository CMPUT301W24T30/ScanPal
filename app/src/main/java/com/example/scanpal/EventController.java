package com.example.scanpal;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles operations related to event management in a Firestore database.
 */
public class EventController {

    private final FirebaseFirestore database; // instance of the database
    private final FirebaseStorage storage;

    /**
     * Constructs an EventController with a reference to a Firestore database.
     *
     *
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
        // will be responsible for add all user types to the database
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
        Bitmap qr_to_event = QrCodeController.generate(event.getId());
        event.setQrToEvent(qr_to_event); // setting bitmap in event to generated qr code

        // Storing the bitmap into firebase by converting into byte array
        byte[] imageDataEvent = QrCodeController.bitmapToByteArray(qr_to_event);

        // Creating bitmap for qrcode checkin
        Bitmap qr_to_checkin = QrCodeController.generate(event.getId());
        event.setQrToCheckIn(qr_to_checkin); // setting bitmap in event to generated qr code

        // Storing the bitmap into firebase by converting into byte array
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
                            .addOnSuccessListener(taskSnapshot -> {
                                // qr code is uploaded to the storage; get download url

                                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                                    eventMap.put("checkInQRCodeUrl", uri.toString());
                                    // Save to database
                                    eventRef.update("checkInQRCodeURL", uri);
                                }).addOnFailureListener(exception -> Log.e("FirebaseStorage",
                                        "Failed to get download url for check in qr code" + exception.getMessage()));
                            });
                    uploadTask = eventQRCodeRef.putBytes(imageDataEvent, metadata);
                    uploadTask
                            .addOnFailureListener(exception -> Log.e("FirebaseStorage",
                                    "Failed to upload event qr code: " + exception.getMessage()))

                            .addOnSuccessListener(taskSnapshot -> {
                                // qr code is uploaded to the storage; get download url

                                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                                    eventMap.put("eventQRCodeUrl", uri.toString());
                                    // Save to database
                                    eventRef.update("eventQRCodeURL", uri);
                                }).addOnFailureListener(exception -> Log.e("FirebaseStorage",
                                        "Failed to get download url for event qr code" + exception.getMessage()));
                            });
                })
                .addOnFailureListener(e -> Log.d("EventController", "Error adding event: " + e.getMessage()));

        // stuff below here is related to uploading the event image
        StorageReference eventPosterRef = storageRef.child("/" + event.getId() + "-poster.jpg");// TODO: checking img
                                                                                                // types?
        UploadTask uploadPhotoTask = eventPosterRef.putFile(event.getPosterURI());

        uploadPhotoTask = eventPosterRef.putFile(event.getPosterURI());

        uploadPhotoTask.addOnSuccessListener(taskSnapshot -> {
            // Photo is uploaded to the storage; get download URL
            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                // Update eventMap with the photo URL
                eventMap.put("photo", uri.toString());

                // Update the event document in Firestore with the photo URL
                eventRef.update("photo", uri.toString())
                        .addOnSuccessListener(aVoid -> {
                            Log.d("EventController", "Photo URL added successfully!");
                        })
                        .addOnFailureListener(exception -> {
                            Log.e("EventController", "Failed to update photo URL: " + exception.getMessage());
                        });
            }).addOnFailureListener(exception -> {
                Log.e("FirebaseStorage", "Failed to get download URL for event photo: " + exception.getMessage());
            });
        }).addOnFailureListener(exception -> {
            Log.e("FirebaseStorage", "Failed to upload event photo: " + exception.getMessage());
        });

    }

    /**
     *
     * This will give back an arrayList of event Type, consisting of events created
     * by the current user (user is obtained from usercontroller.getuser())
     *
     *
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
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                if (task.isSuccessful()) {
                                    // Log.d("RESULT", task.getResult().)

                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        // loop through all events to get events that match
                                        Log.d("E ORGANIZER", document.get("name").toString());

                                        getEventById(document.getId().toString(), new EventFetchCallback() {
                                            @Override
                                            public void onSuccess(Event event) {
                                                userEvents.add(event);
                                                Log.d("INGETBYID CALLBACK", event.getName());
                                                Log.d("eventSIZE", Integer.toString(userEvents.size()));
                                                callback.onSuccess(userEvents);
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Log.d("INGETBYID", "ERROR");
                                            }
                                        });

                                        Log.d("EVENTTRACKING", document.getId().toString());

                                    }
                                } else {
                                    Log.d("EVENTTRACKING", "NOT SUCCESSFUL");
                                    // resolve errors
                                }
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
     *
     * gives an event object from database given its ID
     * 
     * @param EventID  the events document ID
     * @param callback the organizer
     */
    public void getEventById(String EventID, EventFetchCallback callback) {

        database.collection("Events").document(EventID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Log.d("GETBYEID", "IN ON COMPLETE");
                        if (task.isSuccessful()) {
                            DocumentSnapshot eventDoc = task.getResult();
                            if (eventDoc.exists()) {
                                // Document exists, retrieve its data
                                Log.d("GETBYEID", "Document found with ID: " + EventID);

                                // null user is set a few lines later
                                Event event = new Event(null, eventDoc.get("name").toString(),
                                        eventDoc.get("description").toString());

                                event.setLocation(eventDoc.get("location").toString());
                                // event.setInfoAddress( eventDoc.get("eventQRCodeURL").toString() );//assuming
                                // address to the info?
                                // event.setSignUpAddress( eventDoc.get("checkInQRCodeURL").toString()
                                // );//address to qrcheck in?
                                event.setMaximumAttendees((long) eventDoc.get("capacity"));

                                // if event doesn't have an image this will cause a crash
                                Uri imageURI = Uri.parse(eventDoc.get("photo").toString());
                                event.setPosterURI(imageURI);
                                event.setId(EventID);

                                // Access the data using eventDoc.getData() or convert it to an object
                                fetchEventOrganizerByRef((DocumentReference) eventDoc.get("organizer"),
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
                    }
                });

    }

    /**
     *
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

        Map<String, Object> eventMap = new HashMap<>();

        StorageReference storageRef = storage.getReference();
        DocumentReference eventRef = database.collection("Events").document(EventID);

        StorageReference eventPosterRef = storageRef.child("/" + EventID + "-poster.jpg");// TODO: checking img types?
        UploadTask uploadPhotoTask = eventPosterRef.putFile(event.getPosterURI());

        uploadPhotoTask = eventPosterRef.putFile(event.getPosterURI());

        uploadPhotoTask.addOnSuccessListener(taskSnapshot -> {
            // Photo is uploaded to the storage; get download URL
            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                // Update eventMap with the photo URL
                eventMap.put("photo", uri.toString());

                // Update the event document in Firestore with the photo URL
                eventRef.update("photo", uri.toString())
                        .addOnSuccessListener(aVoid -> {
                            Log.d("EventController", "Photo URL added successfully!");
                        })
                        .addOnFailureListener(exception -> {
                            Log.e("EventController", "Failed to update photo URL: " + exception.getMessage());
                        });
            }).addOnFailureListener(exception -> {
                Log.e("FirebaseStorage", "Failed to get download URL for event photo: " + exception.getMessage());
            });
        }).addOnFailureListener(exception -> {
            Log.e("FirebaseStorage", "Failed to upload event photo: " + exception.getMessage());
        });

        database.collection("Events").document(EventID)
                .update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("DB", "Document successfully updated!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DB ERROR", "Error updating document", e);

                    }
                });

    }

    public void fetchEventOrganizerByRef(DocumentReference eventRef, UserFetchCallback callback) {

        eventRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot organizerDoc = task.getResult();

                    if (organizerDoc.exists()) {
                        User organizer = new User(organizerDoc.getId().toString(),
                                organizerDoc.get("firstName").toString(), organizerDoc.get("lastName").toString());
                        organizer.setAdministrator((boolean) organizerDoc.get("administrator"));// CASTING MAY CAUSE
                                                                                                // ISSUES?
                        organizer.setPhoto(organizerDoc.get("photo").toString());

                        callback.onSuccess(organizer);
                    }
                }
            }
        });

    }

}