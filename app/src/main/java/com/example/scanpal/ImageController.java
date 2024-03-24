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
     * Uploads an image file to Firebase Storage within a specified folder.
     *
     * @param fileUri           The URI of the file to be uploaded.
     * @param folderPath        The folder path within Firebase Storage where the file should be stored.
     * @param fileName          The name of the file in storage.
     * @param onSuccessListener Listener for successful upload operations.
     * @param onFailureListener Listener for failed upload operations.
     */
    public void uploadImage(Uri fileUri, String folderPath, String fileName, OnSuccessListener<Uri> onSuccessListener, OnFailureListener onFailureListener) {
        StorageReference imageRef = storage.getReference().child(folderPath + "/" + fileName);
        UploadTask uploadTask = imageRef.putFile(fileUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener))
                .addOnFailureListener(onFailureListener);
    }

    /**
     * Fetches the download URL of an image stored in a specific folder in Firebase Storage.
     *
     * @param folderPath        The folder path within Firebase Storage where the file is stored.
     * @param fileName          The name of the file in storage.
     * @param onSuccessListener Listener for successful upload operations.
     * @param onFailureListener Listener for failed upload operations.
     */
    public void fetchImage(String folderPath, String fileName, OnSuccessListener<Uri> onSuccessListener, OnFailureListener onFailureListener) {
        StorageReference imageRef = storage.getReference().child(folderPath + "/" + fileName);
        imageRef.getDownloadUrl().addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
    }

    /**
     * Deletes a given image stored in a specific folder in Firebase Storage.
     *
     * @param folderPath The folder path within Firebase Storage where the file is stored.
     * @param fileName   The name of the file in storage.
     */
    public void deleteImage(String folderPath, String fileName) {
        StorageReference imageRef = storage.getReference().child(folderPath + "/" + fileName);
        imageRef.delete();
    }
}
