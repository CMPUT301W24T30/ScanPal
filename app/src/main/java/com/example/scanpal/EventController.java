package com.example.scanpal;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
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
     * @param database The Firestore database instance used for event operations.
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

        DocumentReference organizerRef = database.collection("Users").document(event.getOrganizer().getUsername());
        eventMap.put("organizer", organizerRef);

        List<DocumentReference> participantRefs = new ArrayList<>();
        for (Attendee participant : event.getParticipants()) {
            DocumentReference participantRef = database.collection("Attendees").document(participant.getUser().getUsername());
            participantRefs.add(participantRef);
        }
        eventMap.put("participants", participantRefs);

        // Creating bitmap for qrcode and add it to event
        Bitmap qr_to_event = QrCodeController.generate(event.getId());
        event.setQrToEvent(qr_to_event);  // setting bitmap in event to generated qr code

        // Storing the bitmap into firebase by converting into byte array
        byte[] imageDataEvent = QrCodeController.bitmapToByteArray(qr_to_event);

        // Creating bitmap for qrcode checkin
        Bitmap qr_to_checkin = QrCodeController.generate(event.getId());
        event.setQrToCheckIn(qr_to_checkin);  // setting bitmap in event to generated qr code

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
                    UploadTask uploadTask = checkInQRCodeRef.putBytes(imageDataCheckin, metadata);
                    uploadTask
                            .addOnFailureListener(exception -> Log.e("FirebaseStorage", "Failed to upload check in qr code: " + exception.getMessage()))
                            .addOnSuccessListener(taskSnapshot -> {
                                // qr code is uploaded to the storage; get download url

                                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                                    eventMap.put("checkInQRCodeUrl", uri.toString());
                                    // Save to database
                                    eventRef.update("checkInQRCodeURL", uri);
                                }).addOnFailureListener(exception -> Log.e("FirebaseStorage", "Failed to get download url for check in qr code" + exception.getMessage()));
                            });
                    uploadTask = eventQRCodeRef.putBytes(imageDataEvent, metadata);
                    uploadTask
                            .addOnFailureListener(exception -> Log.e("FirebaseStorage", "Failed to upload event qr code: " + exception.getMessage()))
                            .addOnSuccessListener(taskSnapshot -> {
                                // qr code is uploaded to the storage; get download url

                                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                                    eventMap.put("eventQRCodeUrl", uri.toString());
                                    // Save to database
                                    eventRef.update("eventQRCodeURL", uri);
                                }).addOnFailureListener(exception -> Log.e("FirebaseStorage", "Failed to get download url for event qr code" + exception.getMessage()));
                            });


                })
                .addOnFailureListener(e -> Log.d("EventController", "Error adding event: " + e.getMessage()));






    }

    /**
     *
     * @return A list of events created by the Current Instance User
     *
     */
    public void getEventsByUser(View view, EventFetchByUserCallback callback) {
        //eventually a clause for admins to return all existing events

        ArrayList<Event> userEvents = new ArrayList<>();

        UserController userController = new UserController(FirebaseFirestore.getInstance(),view.getContext());
        userController.getUser(userController.fetchStoredUsername(), new UserFetchCallback() {
            @Override
            public void onSuccess(User user) {
                Log.d("LISTENER","IN COMPLETE LISTENER");
                Log.d("USERNAME", "/Users/" + user.getUsername());

                DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users").document(user.getUsername());

                database.collection("Events").whereEqualTo("organizer",userRef )
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                if(task.isSuccessful()) {
                                    Log.d("EVENTTRACKING", "PRELOOP");
                                    //Log.d("RESULT", task.getResult().)

                                    for(QueryDocumentSnapshot document : task.getResult()) {
                                        //loop through all events to get events that match
                                        //DocumentReference userRef;
                                        Log.d("E ORGANIZER", document.get("name").toString() );

                                        getEventById(document.getId().toString(), user, new EventFetchCallback() {
                                            @Override
                                            public void onSuccess(Event event) {
                                                userEvents.add(event);
                                                Log.d("INGETBYID CALLBACK", event.getName() );
                                                Log.d("eventSIZE", Integer.toString(userEvents.size()));
                                                callback.onSuccess(userEvents);
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Log.d("INGETBYID", "ERROR");
                                            }
                                        });

                                        //userEventsIDs.add( getEventfromDB(document.getId().toString()), user );



                                        Log.d("EVENTTRACKING", document.getId().toString());
                                        //DocumentReference eventref = (DocumentReference) document.get("organizer");
                                        //userRef.getId();


                                    }
                                } else {
                                    Log.d("EVENTTRACKING","NOT SUCCESSFUL");
                                    Log.d("EVENTTRACKING",task.getException().getMessage());
                                    //resolve errors
                                }
                                //callback.onSuccess(userEvents);
                                Log.d("EVENTTRACKING", "POSTLOOP");
                            }
                        });
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(view.getContext(), "Failed to fetch User", Toast.LENGTH_LONG).show();
                Log.d("USERERROR", "NO FETCH");
            }
        });

    }

    /**
     *
     * Returns an event from database given its ID
     * @param EventID the events document D
     * @param user the specific user
     */
    private void getEventById(String EventID,User user, EventFetchCallback callback) {

        //Event event;

        database.collection("Events").document(EventID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Log.d("GETBYEID", "IN ON COMPLETE");
                        if(task.isSuccessful()) {
                            DocumentSnapshot eventDoc = task.getResult();
                            if (eventDoc.exists()) {
                                // Document exists, retrieve its data
                                Log.d("GETBYEID", "Document found with ID: " + EventID);
                                // Access the data using eventDoc.getData() or convert it to an object

                                 Event event = new Event(user,eventDoc.get("name").toString(),eventDoc.get("description").toString());// eventDoc.toObject(Event.class);
                                event.setId(EventID);

                                //Log.d("GETBYEID",event.getName());

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

}