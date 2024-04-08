package com.example.scanpal.ControllersTest;

import com.example.scanpal.Controllers.EventController;
import com.example.scanpal.Controllers.QrCodeController;
import com.example.scanpal.Models.Event;
import com.example.scanpal.Models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml")
public class EventControllerTest {

    @Mock
    private FirebaseFirestore mockedFirestore;

    private EventController eventController;

    // In your tests
    @Before
    public void setUp() {
        // Mock FirebaseFirestore and its chain of calls
        FirebaseFirestore mockedFirestore = Mockito.mock(FirebaseFirestore.class);
        CollectionReference mockedCollection = Mockito.mock(CollectionReference.class);
        DocumentReference mockedDocument = Mockito.mock(DocumentReference.class);
        Task<Void> mockTask = Tasks.forResult(null); // Mocking a successful task

        // When Firestore.collection() is called, return the mocked CollectionReference
        when(mockedFirestore.collection(anyString())).thenReturn(mockedCollection);

        // When CollectionReference.document() is called, return the mocked DocumentReference
        when(mockedCollection.document(anyString())).thenReturn(mockedDocument);

        // If you're using DocumentReference.set() in your EventController.addEvent(), mock its return as well
        // This assumes that set() is called with some object and returns a Task<Void>
        when(mockedDocument.set(any())).thenReturn(mockTask);

        // Mock QrCodeController as before, ensure to stub any methods of QrCodeController used by EventController
        QrCodeController mockedQrCodeController = Mockito.mock(QrCodeController.class);

        // Initialize EventController with the mocked dependencies
        eventController = new EventController(mockedFirestore, mockedQrCodeController);
    }




    @Test
    public void addEvent_ShouldCallFirestore() {
        // Arrange
        User dummyOrganizer = new User();
        String eventName = "Test Event";
        String eventDescription = "This is a test description for the Test Event.";

        Event dummyEvent = new Event(dummyOrganizer, eventName, eventDescription);
        String dummyID = "testEventID";

        // Act
        eventController.addEvent(dummyEvent, dummyID);

        // Assert
        verify(mockedFirestore, times(1)).collection("Events");
    }

}
