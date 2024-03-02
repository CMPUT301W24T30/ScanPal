package com.example.scanpal;

import android.net.Uri;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ImageController {
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    public void uploadImage(Uri fileUri, OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener, OnFailureListener onFailureListener) {
        String fileName = System.currentTimeMillis() + ".jpg";

        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child(fileName);
        UploadTask uploadTask = imageRef.putFile(fileUri);

        uploadTask.addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
    }

    public void fetchImage(String fileName, OnSuccessListener<Uri> onSuccessListener, OnFailureListener onFailureListener) {
        StorageReference storageRef = storage.getReference().child(fileName);

        storageRef.getDownloadUrl().addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
    }
}
