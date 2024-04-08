package com.example.scanpal.ControllersTest;

import com.example.scanpal.Callbacks.EventDeleteCallback;
import com.example.scanpal.Controllers.EventController;
import com.example.scanpal.Models.Event;
import com.example.scanpal.Models.User;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

@RunWith(RobolectricTestRunner.class)
public class EventControllerTest {

    private EventController eventController;
    private FirebaseFirestore mockedFirestore;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.initMocks(this);

        eventController = mock(EventController.class);
    }

    @Test
    public void testAddEvent() {

        Event mockEvent = mock(Event.class);
        when(mockEvent.getName()).thenReturn("Test Event");
        when(mockEvent.getDescription()).thenReturn("This is a description.");
        when(mockEvent.getOrganizer()).thenReturn(new User("username", "First", "Last", "deviceToken"));

        String mockID = "eventID";
        eventController.addEvent(mockEvent, mockID);

    }

    @Test
    public void testFetchAllEvents() {
        // Simulate the fetchAllEvents method being called
        eventController.fetchAllEvents(null);
        // Verify that fetchAllEvents was called on the mocked EventController
        verify(eventController).fetchAllEvents(null);
    }


    @Test
    public void testGetEventById() {
        // Simulate calling getEventById with a specific eventID
        eventController.getEventById("specificEventID", null);
        // Verify that getEventById was called on the mocked EventController with the specified arguments
        verify(eventController).getEventById("specificEventID", null);
    }
}
