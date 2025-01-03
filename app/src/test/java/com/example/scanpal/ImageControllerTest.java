package com.example.scanpal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.net.Uri;

import com.example.scanpal.Controllers.ImageController;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {34})
public class ImageControllerTest {
    @Mock
    private FirebaseStorage storage;
    private ImageController imageController;
    @Mock
    private StorageReference storageReference;
    @Mock
    private UploadTask uploadTask;

    @Before
    public void setUp() {
        storage = mock(FirebaseStorage.class);
        storageReference = mock(StorageReference.class);
        uploadTask = mock(UploadTask.class);
        
        Task<Uri> mockDownloadUrlTask = mock(Task.class);
        StorageTask<UploadTask.TaskSnapshot> mockUploadTask = mock(StorageTask.class);
        
        when(storage.getReference()).thenReturn(storageReference);
        when(storageReference.child(anyString())).thenReturn(storageReference);
        when(storageReference.getDownloadUrl()).thenReturn(mockDownloadUrlTask);
        when(storageReference.putFile(any(Uri.class))).thenReturn(uploadTask);
        when(uploadTask.addOnSuccessListener(any())).thenReturn(mockUploadTask);
        when(uploadTask.addOnFailureListener(any())).thenReturn(mockUploadTask);
        when(mockDownloadUrlTask.addOnSuccessListener(any())).thenReturn(mockDownloadUrlTask);
        when(mockDownloadUrlTask.addOnFailureListener(any())).thenReturn(mockDownloadUrlTask);

        imageController = new ImageController(storage);
    }

    @Test
    public void testUploadImage() {
        Uri mockUri = mock(Uri.class);
        imageController.uploadImage(mockUri, "folderPath", "fileName", mock(OnSuccessListener.class), mock(OnFailureListener.class));
        verify(storageReference, times(1)).putFile(mockUri);
    }

    @Test
    public void testFetchImage() {
        imageController.fetchImage("folderPath", "fileName", mock(OnSuccessListener.class), mock(OnFailureListener.class));
        verify(storageReference, times(1)).getDownloadUrl();
    }

    @Test
    public void testDeleteImage() {
        imageController.deleteImage("folderPath", "fileName");
        verify(storageReference, times(1)).delete();
    }
}
