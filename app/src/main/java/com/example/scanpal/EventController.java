package com.example.scanpal;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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


public class EventController {

    private final FirebaseFirestore database; //instance of the database

    public EventController(FirebaseFirestore database) {
        this.database = database;
    }

    public FirebaseFirestore getDatabase() {
        return this.database;
    }

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

        // Save to database
        database.collection("Events").document(event.getId()).set(eventMap)
                .addOnSuccessListener(aVoid -> System.out.println("Event added successfully!"))
                .addOnFailureListener(e -> System.out.println("Error adding event: " + e.getMessage()));

        // Try creating bitmap for qrcode
        try {
            event.setQr(generateQRCode(event.getId()));  // try setting bitmap in event to generated qr code
        } catch (WriterException e) {
            System.err.println("Error generating QR Code for event " + event.getId() + ": " + e.getMessage());  // throw writing bitmap error otherwise
        }
    }


    public Bitmap generateQRCode(String data) throws WriterException {  // the data is the event id
        int width = 300;
        int height = 300;

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        return bitmap;
    }
}

