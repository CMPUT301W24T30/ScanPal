package com.example.scanpal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

import com.example.scanpal.Callbacks.EventDeleteCallback;
import com.example.scanpal.Controllers.EventController;
import com.example.scanpal.Controllers.ImageController;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.TaskState;

import java.util.Collection;

@RunWith(MockitoJUnitRunner.class)
public class EventControllerTest {
    @Mock
    private ImageController imageController;
    @Mock
    private FirebaseFirestore database;
    @Mock
    private DocumentReference documentReference;
    @Mock
    private FirebaseStorage storage;
    @Mock
    private EventDeleteCallback eventDeleteCallback;
    private EventController eventController;
    @Mock
    private Task<Void> mockDeleteTask;


    @Before
    public void setUp() {
        database = mock(FirebaseFirestore.class);
        documentReference = mock(DocumentReference.class);
        eventDeleteCallback = mock(EventDeleteCallback.class);
        CollectionReference collectionReference = mock(CollectionReference.class);
        mockDeleteTask = mock(Task.class);

        when(database.collection(any())).thenReturn(collectionReference);
        when(collectionReference.document(any())).thenReturn(documentReference);
        when(documentReference.delete()).thenReturn(mockDeleteTask);
        when(mockDeleteTask.addOnSuccessListener(any())).thenReturn(mockDeleteTask);
        when(mockDeleteTask.addOnFailureListener(any())).thenReturn(mockDeleteTask);
    }

    /**
     * Test the deleteEvent method in the EventController class.
     */
    @Test
    public void testDeleteEvent() {
        String eventID = "123";
        eventController = new EventController(database, storage, imageController);

        // Mock the delete operation to return a successful task
        doAnswer(invocation -> {
            OnSuccessListener<Void> onSuccess = invocation.getArgument(0);
            onSuccess.onSuccess(null);
            return mockDeleteTask;
        }).when(mockDeleteTask).addOnSuccessListener(any());

        eventController.deleteEvent(eventID, eventDeleteCallback);

        verify(imageController, times(1)).deleteImage("events", "event_" + eventID + ".jpg");
        verify(imageController, times(1)).deleteImage("qr-codes", eventID + "-check-in.png");
        verify(imageController, times(1)).deleteImage("qr-codes", eventID + "-event.png");
        verify(eventDeleteCallback, times(1)).onSuccess();
        verify(eventDeleteCallback, never()).onError(any());
    }

    /**
     * Test the deleteEvent method in the EventController class when the delete operation fails.
     */
    @Test
    public void testDeleteEventFail() {
        String eventID = "123";
        eventController = new EventController(database, storage, imageController);

        // Mock the delete operation to return a failed task
        doAnswer(invocation -> {
            OnFailureListener onFailureListener = invocation.getArgument(0);
            onFailureListener.onFailure(null);
            return mockDeleteTask;
        }).when(mockDeleteTask).addOnFailureListener(any());

        eventController.deleteEvent(eventID, eventDeleteCallback);

        verify(imageController, times(1)).deleteImage("events", "event_" + eventID + ".jpg");
        verify(imageController, times(1)).deleteImage("qr-codes", eventID + "-check-in.png");
        verify(imageController, times(1)).deleteImage("qr-codes", eventID + "-event.png");
        verify(eventDeleteCallback, never()).onSuccess();
        verify(eventDeleteCallback, times(1)).onError(any());
    }

}
