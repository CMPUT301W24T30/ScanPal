package com.example.scanpal;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public EventController(FirebaseFirestore database) {
        // Why not just call FirebaseFireStore.getInstance()?
        this.database = database;
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
     * Adds a new event to the Firestore database. Uploads the event qr code image first then uploads to Firestore.
     *
     * TODO still need to upload event promotion qrcode image.
     *
     * @param event The event to be added to the database.
     */
    public void addEvent(Event event) {
        // will be responsible for add all user types to the database
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("name", event.getName());
        eventMap.put("description", event.getDescription());

        DocumentReference organizerRef = database.collection("Users").document(event.getOrganizer().getUsername());
        eventMap.put("organizer", organizerRef);

        List<DocumentReference> participantRefs = new ArrayList<>();
        for (Attendee participant : event.getParticipants()) {
            DocumentReference participantRef = database.collection("Attendees")
                    .document(participant.getUser().getUsername());
            participantRefs.add(participantRef);
        }
        eventMap.put("participants", participantRefs);

        // upload qr code image
        StorageReference storageRef = storage.getReference();
        StorageReference qrCodesRef = storageRef.child("qr-codes/" + event.getId() + ".png");
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/png")
                .build();
        UploadTask uploadTask = qrCodesRef.putBytes(event.getEventQRCodeByteArray(), metadata);
        uploadTask
                .addOnFailureListener(exception -> Log.e("FirebaseStorage", "Failed to upload qr code: " + exception.getMessage()))
                .addOnSuccessListener(taskSnapshot -> {
                    // qr code is uploaded to the storage; get download url

                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        eventMap.put("eventQRCodeUrl", uri.toString());
                        // Save to database
                        database.collection("Events").document(event.getId()).set(eventMap)
                                .addOnSuccessListener(aVoid -> System.out.println("Event added successfully!"))
                                .addOnFailureListener(e -> System.out.println("Error adding event: " + e.getMessage()));
                    }).addOnFailureListener(exception -> Log.e("FirebaseStorage", "Failed to get download url for event qr code" + exception.getMessage()));
                });
    }
}
