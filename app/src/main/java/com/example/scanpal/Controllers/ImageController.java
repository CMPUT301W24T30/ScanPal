package com.example.scanpal.Controllers;

import android.net.Uri;

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
     * Fetches all image URLs from a specific folder in Firebase Storage.
     *
     * @param folderPath        The folder path within Firebase Storage.
     * @param onSuccessListener Listener for successful operations, receiving a list of image URLs.
     * @param onFailureListener Listener for failed operations.
     */
    public void fetchAllImages(String folderPath, OnSuccessListener<List<Uri>> onSuccessListener, OnFailureListener onFailureListener) {
        StorageReference folderRef = storage.getReference().child(folderPath);

        folderRef.listAll()
                .addOnSuccessListener(listResult -> {
                    List<Uri> imageUrls = new ArrayList<>();
                    List<StorageReference> items = listResult.getItems();

                    // Track the number of successful fetches to know when all URLs have been loaded
                    final int[] successfulFetches = {0};

                    if (items.isEmpty()) {
                        // Immediately return an empty list if the folder has no items
                        onSuccessListener.onSuccess(imageUrls);
                    } else {
                        for (StorageReference itemRef : items) {
                            itemRef.getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        imageUrls.add(uri);
                                        successfulFetches[0]++;
                                        // Call the success listener once all URLs are fetched
                                        if (successfulFetches[0] == items.size()) {
                                            onSuccessListener.onSuccess(imageUrls);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        onFailureListener.onFailure(e);
                                        // Optionally, you could also collect and report back individual failures
                                    });
                        }
                    }
                })
                .addOnFailureListener(onFailureListener);
    }
}
