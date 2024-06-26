package com.example.scanpal.Controllers;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.example.scanpal.Models.Event;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles QR Code generation and aids in storing QR Codes
 */

public class QrCodeController {
    /**
     * Generates a qr code and returns in in the form of a bitmap
     */

    private final FirebaseFirestore database;
    private final FirebaseStorage storage;

    /**
     * Constructs an EventController with a reference to a Firestore database.
     */
    public QrCodeController() {
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    /**
     * Generates a QR Code bitmap from the provided data.
     * Generates a QR Code bitmap with the specified data using the ZXing library.
     * The generated QR Code has a default width and height of 300 pixels.
     *
     * @param data The data to be encoded in the QR Code.
     * @return A Bitmap object representing the generated QR Code, or null if an error occurs during generation.
     */
    public static Bitmap generate(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        int width = 300;
        int height = 300;

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;

        } catch (WriterException e) {
            // Handle the exception
            System.err.println("Error generating QR Code for event ");
            return null;
        }
    }

    /**
     * Converts a Bitmap to a byte array
     *
     * @param bitmap The Bitmap to convert
     * @return A byte array representing the Bitmap
     */
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    /**
     * Generates and Qr Code in Database
     *
     * @param event    The event to link to
     * @param eventMap the map of the event as stored in the database
     */
    public void generateAndStoreQrCode(Event event, Map<String, Object> eventMap) {
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
                    UploadTask uploadTask = checkInQRCodeRef.putBytes(imageDataCheckin, metadata);
                    uploadTask
                            .addOnFailureListener(exception -> Log.e("FirebaseStorage",
                                    "Failed to upload check in qr code: " + exception.getMessage()))
                            .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                                eventMap.put("checkInQRCodeUrl", uri.toString());
                                eventRef.update("checkInQRCodeURL", uri);
                            }).addOnFailureListener(exception -> Log.e("FirebaseStorage",
                                    "Failed to get download url for check in qr code" + exception.getMessage())));
                    uploadTask = eventQRCodeRef.putBytes(imageDataEvent, metadata);
                    uploadTask
                            .addOnFailureListener(exception -> Log.e("FirebaseStorage",
                                    "Failed to upload event qr code: " + exception.getMessage()))

                            .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                                eventMap.put("eventQRCodeUrl", uri.toString());
                                eventRef.update("eventQRCodeURL", uri);
                            }).addOnFailureListener(exception -> Log.e("FirebaseStorage",
                                    "Failed to get download url for event qr code" + exception.getMessage())));
                })
                .addOnFailureListener(e -> Log.d("EventController", "Error adding event: " + e.getMessage()));
    }
}


