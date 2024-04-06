package com.example.scanpal.Controllers;

import android.net.Uri;

import com.example.scanpal.Callbacks.ImagesDeleteCallback;
import com.example.scanpal.Callbacks.ImagesFetchCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

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
     * Fetches the download URL of an image stored in a specific folder in Firebase Storage.
     *
     * @param folderPath        The folder path within Firebase Storage where the file is stored.
     * @param onSuccessListener Listener for successful upload operations.
     * @param onFailureListener Listener for failed upload operations.
     */
    public void fetchImage(String folderPath, OnSuccessListener<Uri> onSuccessListener, OnFailureListener onFailureListener) {
        StorageReference imageRef = storage.getReference().child(folderPath);
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

    /**
     * Deletes a given image stored in a specific folder in Firebase Storage.
     *
     * @param filePath The folder path within Firebase Storage where the file is stored.
     */
    public void deleteImage(String filePath, final ImagesDeleteCallback callback) {

        StorageReference imageRef = storage.getReference().child(filePath);
        imageRef.delete().addOnSuccessListener(result -> callback.onSuccess()).addOnFailureListener(callback::onError);
        ;
    }


    /**
     * Fetches all image URLs from a specific folder in Firebase Storage.
     *
     * @param callback Callback for handling the fetched image URLs.
     */
    public void fetchAllImages(final ImagesFetchCallback callback) {
        List<String> imageUrls = new ArrayList<>();
        StorageReference imageRef = storage.getReference();

        imageRef.listAll().addOnSuccessListener(result -> {
            if (result.getItems().isEmpty() && result.getPrefixes().isEmpty()) {
                // Directly call onSuccess if there are no items and no prefixes
                callback.onSuccess(imageUrls);
                return;
            }

            for (StorageReference item : result.getItems()) {
                String path = item.getPath();
                imageUrls.add(path);
            }

            // Update for each prefix
            for (StorageReference prefix : result.getPrefixes()) {

                prefix.listAll().addOnSuccessListener(folder -> {
                    for (StorageReference item : folder.getItems()) {
                        String path = item.getPath();
                        imageUrls.add(path);
                    }

                    callback.onSuccess(imageUrls);

                }).addOnFailureListener(callback::onError);
            }
        }).addOnFailureListener(callback::onError);
    }


}
