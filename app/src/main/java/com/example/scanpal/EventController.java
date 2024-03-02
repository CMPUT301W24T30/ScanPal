package com.example.scanpal;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;


/**
 * Handles operations related to event management in a Firestore database.
 */
public class EventController {

    private final FirebaseFirestore database; // instance of the database

    /**
     * Constructs an EventController with a reference to a Firestore database.
     *
     * @param database The Firestore database instance used for event operations.
     */
    public EventController(FirebaseFirestore database) {
        this.database = database;
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
        eventMap.put("name", event.getName());
        eventMap.put("description", event.getDescription());

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
        eventMap.put("QrCode for event", imageDataEvent);

        // Creating bitmap for qrcode checkin
        Bitmap qr_to_checkin = QrCodeController.generate(event.getId());
        event.setQrToEvent(qr_to_checkin);  // setting bitmap in event to generated qr code

        // Storing the bitmap into firebase by converting into byte array
        byte[] imageDataCheckin = QrCodeController.bitmapToByteArray(qr_to_checkin);
        eventMap.put("QrCode for checkin", imageDataCheckin);

        // Save to database
        database.collection("Events").document(event.getId()).set(eventMap)
                .addOnSuccessListener(aVoid -> System.out.println("Event added successfully!"))
                .addOnFailureListener(e -> System.out.println("Error adding event: " + e.getMessage()));

    }

}