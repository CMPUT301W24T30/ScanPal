package com.example.scanpal;

import android.net.Uri;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * Controller class for managing image uploads and retrievals with Firebase Storage.
 */
public class ImageController {
    protected FirebaseStorage storage = FirebaseStorage.getInstance();

    /**
     * Uploads an image file to Firebase Storage.
     *
     * @param fileUri           The URI of the file to be uploaded.
     * @param onSuccessListener Listener for successful upload operations.
     * @param onFailureListener Listener for failed upload operations.
     */
    public void uploadImage(Uri fileUri, OnSuccessListener<Uri> onSuccessListener, OnFailureListener onFailureListener) {
        String fileName = "profiles/" + System.currentTimeMillis() + ".jpg";

        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child(fileName);
        UploadTask uploadTask = imageRef.putFile(fileUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener))
                .addOnFailureListener(onFailureListener);
    }

    /**
     * Fetches the download URL of an image stored in Firebase Storage.
     *
     * @param fileName          The name of the file in storage.
     * @param onSuccessListener Listener for successful retrieval operations.
     * @param onFailureListener Listener for failed retrieval operations.
     */
    public void fetchImage(String fileName, OnSuccessListener<Uri> onSuccessListener, OnFailureListener onFailureListener) {
        StorageReference storageRef = storage.getReference().child(fileName);

        storageRef.getDownloadUrl().addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
    }
}
