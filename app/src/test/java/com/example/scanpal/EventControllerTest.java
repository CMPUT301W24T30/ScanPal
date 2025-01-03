package com.example.scanpal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

import android.net.Uri;
import android.util.Log;

import com.example.scanpal.Callbacks.DeleteAllAttendeesCallback;
import com.example.scanpal.Callbacks.EventDeleteCallback;
import com.example.scanpal.Callbacks.EventIDsFetchCallback;
import com.example.scanpal.Callbacks.UserFetchCallback;
import com.example.scanpal.Controllers.AttendeeController;
import com.example.scanpal.Controllers.EventController;
import com.example.scanpal.Controllers.ImageController;
import com.example.scanpal.Controllers.QrCodeController;
import com.example.scanpal.Models.Event;
import com.example.scanpal.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.TaskState;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.Silent.class)
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
    private CollectionReference collectionReference;
    @Mock
    private AttendeeController attendeeController;
    @Mock
    private QrCodeController qrCodeController;


    @Before
    public void setUp() {
        database = mock(FirebaseFirestore.class);
        documentReference = mock(DocumentReference.class);
        eventDeleteCallback = mock(EventDeleteCallback.class);
        collectionReference = mock(CollectionReference.class);
        attendeeController = mock(AttendeeController.class);
        qrCodeController = mock(QrCodeController.class);
        eventController = new EventController(database, imageController, attendeeController, qrCodeController);
    }

    /**
     * Test the deleteEvent method in the EventController class.
     */
    @Test
    public void testDeleteEvent() {
        Task<Void> mockDeleteTask = mock(Task.class);
        DeleteAllAttendeesCallback deleteAllAttendeesCallback = mock(DeleteAllAttendeesCallback.class);

        when(database.collection(any())).thenReturn(collectionReference);
        when(collectionReference.document(any())).thenReturn(documentReference);
        when(documentReference.delete()).thenReturn(mockDeleteTask);

        doAnswer(invocation -> {
            DeleteAllAttendeesCallback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(attendeeController).deleteAllAttendeesForEvent(any(), any());

        String eventID = "123";

        doAnswer(invocation -> {
            OnSuccessListener<Void> onSuccess = invocation.getArgument(0);
            onSuccess.onSuccess(null);
            return mockDeleteTask;
        }).when(mockDeleteTask).addOnSuccessListener(any(OnSuccessListener.class));

        eventController.deleteEvent(eventID, eventDeleteCallback);

        verify(imageController, times(1)).deleteImage("events", eventID + ".jpg");
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
        Task<Void> mockDeleteTask = mock(Task.class);
        when(database.collection(any())).thenReturn(collectionReference);
        when(collectionReference.document(any())).thenReturn(documentReference);

        doAnswer(invocation -> {
            DeleteAllAttendeesCallback callback = invocation.getArgument(1);
            callback.onError(mock(Exception.class));
            return null;
        }).when(attendeeController).deleteAllAttendeesForEvent(any(), any());

        String eventID = "123";

        eventController.deleteEvent(eventID, eventDeleteCallback);

        verify(imageController, times(0)).deleteImage("events", eventID + ".jpg");
        verify(imageController, times(0)).deleteImage("qr-codes", eventID + "-check-in.png");
        verify(imageController, times(0)).deleteImage("qr-codes", eventID + "-event.png");
        verify(eventDeleteCallback, never()).onSuccess();
        verify(eventDeleteCallback, times(1)).onError(any());
    }

    /**
     * Test the getAllEventIds method in the EventController class.
     */
    @Test
    public void testGetAllEventIds() {
        Task<QuerySnapshot> task = mock(Task.class);
        QuerySnapshot querySnapshot = mock(QuerySnapshot.class);
        EventIDsFetchCallback callback = mock(EventIDsFetchCallback.class);
        QueryDocumentSnapshot documentSnapshot1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot documentSnapshot2 = mock(QueryDocumentSnapshot.class);
        List<QueryDocumentSnapshot> documentSnapshots = Arrays.asList(documentSnapshot1, documentSnapshot2);

        when(database.collection("Events")).thenReturn(collectionReference);
        when(collectionReference.get()).thenReturn(task);
        when(task.addOnCompleteListener(any())).thenAnswer(invocation -> {
            OnCompleteListener<QuerySnapshot> listener = invocation.getArgument(0);
            // simulate a successful task completion
            when(task.isSuccessful()).thenReturn(true);
            when(task.getResult()).thenReturn(querySnapshot);
            // manually trigger the listener with the mocked task
            listener.onComplete(task);
            return null;
        });
        when(task.isSuccessful()).thenReturn(true);
        when(task.getResult()).thenReturn(querySnapshot);
        when(querySnapshot.iterator()).thenReturn(documentSnapshots.iterator());
        when(documentSnapshot1.getId()).thenReturn("id1");
        when(documentSnapshot2.getId()).thenReturn("id2");

        eventController.getAllEventIds(callback);

        // Verify that the callback is called with the correct event IDs
        ArgumentCaptor<List<String>> argumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(callback).onSuccess(argumentCaptor.capture());
        List<String> eventIDs = argumentCaptor.getValue();

        assertEquals(2, eventIDs.size());
        assertTrue(eventIDs.contains("id1"));
        assertTrue(eventIDs.contains("id2"));
    }

    /**
     * Test the getAllEventIds method in the EventController class when the documentReference is null.
     */
    @Test
    public void testFetchEventOrganizerByRef_DocumentReferenceIsNull() {
        UserFetchCallback callback = mock(UserFetchCallback.class);
        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            mockedLog.when(() -> Log.e(anyString(), anyString())).thenReturn(0);
            eventController.fetchEventOrganizerByRef(null, callback);
        }
        verify(callback).onError(any(NullPointerException.class));
    }

    /**
     * Test the fetchEventOrganizerByRef method in the EventController class.
     */
    @Test
    public void testFetchEventOrganizerByRef() {
        DocumentSnapshot documentSnapshot = mock(DocumentSnapshot.class);
        when(documentSnapshot.getString("firstName")).thenReturn("John");
        when(documentSnapshot.getString("lastName")).thenReturn("Doe");
        when(documentSnapshot.getString("homepage")).thenReturn("https://example.com");
        when(documentSnapshot.getString("photo")).thenReturn("uri_photo");
        when(documentSnapshot.getString("deviceToken")).thenReturn("token");
        when(documentSnapshot.getBoolean("administrator")).thenReturn(true);
        when(documentSnapshot.exists()).thenReturn(true);

        DocumentReference eventRef = mock(DocumentReference.class);
        Task<DocumentSnapshot> task = mock(Task.class);
        when(eventRef.get()).thenReturn(task);

        when(task.addOnCompleteListener(any())).thenAnswer(invocation -> {
            OnCompleteListener<DocumentSnapshot> listener = invocation.getArgument(0);
            when(task.isSuccessful()).thenReturn(true);
            when(task.getResult()).thenReturn(documentSnapshot);
            listener.onComplete(task);
            return null;
        });

        UserFetchCallback callback = mock(UserFetchCallback.class);
        eventController.fetchEventOrganizerByRef(eventRef, callback);

        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(callback).onSuccess(argumentCaptor.capture());
        User user = argumentCaptor.getValue();

        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("https://example.com", user.getHomepage());
        assertEquals("uri_photo", user.getPhoto());
        assertEquals("token", user.getDeviceToken());
        assertTrue(user.isAdministrator());
    }

    /**
     * Test addEvent method
     */
    @Test
    public void testAddEvent() {
        Event mockEvent = mock(Event.class);
        User mockUser = mock(User.class);
        String id = "123";

        when(database.collection(any())).thenReturn(collectionReference);
        when(collectionReference.document(any())).thenReturn(documentReference);

        when(mockEvent.getOrganizer()).thenReturn(mockUser);
        when(mockUser.getUsername()).thenReturn(id);

        doAnswer(invocation -> {
            OnSuccessListener<Uri> onSuccess = invocation.getArgument(3);
            onSuccess.onSuccess(mock(Uri.class));
            return null;
        }).when(imageController).uploadImage(any(), any(), any(), any(), any());

        eventController.addEvent(mockEvent, id);
        verify(qrCodeController, times(1)).generateAndStoreQrCode(any(Event.class), any(Map.class));
        verify(documentReference, times(1)).update(anyString(), any());
    }

}
