package com.example.scanpal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.net.Uri;

import com.example.scanpal.Callbacks.ImagesFetchCallback;
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
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
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
        imageController = new ImageController(storage);
        storageReference = mock(StorageReference.class);
        uploadTask = mock(UploadTask.class);

        Task<Uri> mockDownloadUrlTask = mock(Task.class);

        when(storage.getReference()).thenReturn(storageReference);
        when(storageReference.child(anyString())).thenReturn(storageReference);
        when(storageReference.getDownloadUrl()).thenReturn(mockDownloadUrlTask);

        when(storageReference.putFile(any(Uri.class))).thenReturn(uploadTask);

        doAnswer(invocation -> {
            OnSuccessListener<Uri> onSuccessListener = invocation.getArgument(0);
            onSuccessListener.onSuccess(mock(Uri.class));
            return mock(Task.class);
        }).when(mockDownloadUrlTask).addOnSuccessListener(any(OnSuccessListener.class));

        doAnswer(invocation -> {
            OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener = invocation.getArgument(0);
            onSuccessListener.onSuccess(mock(UploadTask.TaskSnapshot.class));
            return mock(StorageTask.class);
        }).when(uploadTask).addOnSuccessListener(any(OnSuccessListener.class));

    }

    @Test
    public void testUploadImage() {

        Uri mockUri = mock(Uri.class);
        imageController.uploadImage(mockUri, "folderPath", "fileName", mock(OnSuccessListener.class), mock(OnFailureListener.class));

        verify(storageReference, times(1)).putFile(mockUri);
        verify(uploadTask, times(1)).addOnSuccessListener(any(OnSuccessListener.class));
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
