package com.example.scanpal.ModelsTest;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.example.scanpal.Models.Attendee;
import com.example.scanpal.Models.Event;
import com.example.scanpal.Models.User;

import java.util.ArrayList;

public class EventTest {

    private Event event;
    private User organizer;

    @Before
    public void setUp() {
        organizer = new User("organizerUsername", "OrganizerFirstName", "OrganizerLastName", "deviceToken");
        event = new Event(organizer, "EventName", "EventDescription");
    }

    @Test
    public void constructor_initializesCorrectly() {
        assertEquals(organizer, event.getOrganizer());
        assertEquals("EventName", event.getName());
        assertEquals("EventDescription", event.getDescription());
        assertTrue(event.getParticipants().isEmpty()); // Participants list should be empty initially
        assertEquals(Long.valueOf(0L), event.getTotalCheckInCount()); // Check-in count should be 0 initially
    }

    @Test
    public void settingAndGettingProperties_worksCorrectly() {
        event.setId("EventID");
        assertEquals("EventID", event.getId());

        event.setLocation("EventLocation");
        assertEquals("EventLocation", event.getLocation());

        event.setMaximumAttendees(100L);
        assertEquals(Long.valueOf(100), event.getMaximumAttendees());

        event.setTrackLocation(true);
        assertTrue(event.isTrackLocation());

        event.setLocationCoords("12.345678,9.876543");
        assertEquals("12.345678,9.876543", event.getLocationCoords());

        ArrayList<Attendee> attendees = new ArrayList<>();
        attendees.add(new Attendee());
        event.setParticipants(attendees);
        assertFalse(event.getParticipants().isEmpty());

        event.setSignUpAddress("SignUpURL");
        assertEquals("SignUpURL", event.getSignUpAddress());


    }

    @Test
    public void userSignUpStatus_isHandledCorrectly() {
        // Initially, assume the user is not signed up for the event
        assertFalse("Initially, user should not be signed up", event.isUserSignedUp());

        // Set the user sign-up status to true and verify
        event.setUserSignedUp(true);
        assertTrue("User sign-up status should be updated to true", event.isUserSignedUp());

        // Set the user sign-up status to false and verify
        event.setUserSignedUp(false);
        assertFalse("User sign-up status should be updated back to false", event.isUserSignedUp());
    }

    @Test
    public void eventDateAndTime_areCorrectlyUpdated() {
        // Define test values for date and time
        String testDate = "2024-01-01";
        String testTime = "12:00 PM";

        // Set the date and time for the event
        event.setDate(testDate);
        event.setTime(testTime);

        // Verify that the date and time are correctly updated
        assertEquals("The event date should match the set value", testDate, event.getDate());
        assertEquals("The event time should match the set value", testTime, event.getTime());
    }

    @Test
    public void totalCheckInCount_isAccuratelyTracked() {
        // Initially, the total check-in count should be zero
        assertEquals("Initial total check-in count should be 0", Long.valueOf(0L), event.getTotalCheckInCount());

        // Simulate a few check-ins and verify the count
        event.setTotalCheckInCount(10L);
        assertEquals("Total check-in count should be updated to 10", Long.valueOf(10L), event.getTotalCheckInCount());

        // Simulate more check-ins and verify the updated count
        event.setTotalCheckInCount(event.getTotalCheckInCount() + 5);
        assertEquals("Total check-in count should be updated to 15", Long.valueOf(15L), event.getTotalCheckInCount());
    }

    @Test
    public void announcementCount_isCorrectlyManaged() {
        // Initially, the announcement count should be zero
        assertEquals("Initial announcement count should be 0", Long.valueOf(0L), event.getAnnouncementCount());

        // Update the announcement count and verify
        event.setAnnouncementCount(5L);
        assertEquals("Announcement count should be updated to 5", Long.valueOf(5L), event.getAnnouncementCount());

        // Increment and verify the announcement count
        event.setAnnouncementCount(event.getAnnouncementCount() + 1);
        assertEquals("Announcement count should be incremented to 6", Long.valueOf(6L), event.getAnnouncementCount());
    }





}

