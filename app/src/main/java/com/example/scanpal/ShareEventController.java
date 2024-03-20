package com.example.scanpal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Handles operations related to sharing an event's qr code
 */
public class ShareEventController {

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final Context context;

    public ShareEventController(Context context) {
        this.context = context;
    }

    /**
     * Function that event page's share button will call by giving the event id
     *
     * @param eventID The ID of the event to be shared
     */
    public void shareQrCode(String eventID){
        // Take event id, get image, and put it into bitmap
        String imageName = eventID + "-event.png";
        StorageReference imageRef = storage.getReference().child("qr-codes/" + imageName);

        // download image into bitmap
        imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // convert to bitmap
                Bitmap imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                shareIntent(imageBitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle error
                Toast.makeText(context, "Error Retrieving Image", Toast.LENGTH_SHORT).show();
            }

        });

    }
    /**
     * Initiates the intent that will pop up the share menu
     *
     * @param imageBitmap The bitmap for the qr code to be shared
     */
    public void shareIntent(Bitmap imageBitmap) {
        Uri uri = getUri(imageBitmap);
        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, "Sharing QR Code");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Join this event!");
        intent.setType("image/png");
        context.startActivity(Intent.createChooser(intent, "Share via"));
    }


    /**
     * Takes the bitmap of the qr code and returns a uri
     *
     * @param imageBitmap The bitmap for the qr code to be shared
     * @return The uri for the image to be shared
     */
    public Uri getUri(Bitmap imageBitmap) {
        File cache = new File(context.getCacheDir(), "images");
        Uri uri = null;

        try {
            cache.mkdirs();
            File image = new File(cache, "shared_image.png");
            // Save bitmap to the temporary file
            FileOutputStream outputStream = new FileOutputStream(image);
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            uri = FileProvider.getUriForFile(context, "com.example.scanpal.fileprovider", image);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error getting URI", Toast.LENGTH_SHORT).show();
        }
        return uri;
    }
}
