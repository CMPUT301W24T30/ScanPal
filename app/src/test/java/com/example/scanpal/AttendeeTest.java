package com.example.scanpal;

import com.example.scanpal.Models.Attendee;
import com.example.scanpal.Models.User;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class AttendeeTest {

    private Attendee attendee;
    private User user;

    @Before
    public void setUp() {
        user = new User("user123", "John", "Doe", "deviceToken123");
        attendee = new Attendee(user, "event123", true, false, 0L);
    }

    @Test
    public void constructor_initializesPropertiesCorrectly() {
        assertEquals(user, attendee.getUser());
        assertEquals("event123", attendee.getEventID());
        assertTrue("RSVP should be true", attendee.isRsvp());
        assertFalse("CheckedIn should be false", attendee.isCheckedIn());
        assertEquals(Long.valueOf(0), attendee.getCheckinCount());
    }

    @Test
    public void setters_updatePropertiesCorrectly() {
        // Create a new User and set it
        User newUser = new User("user456", "Jane", "Doe", "deviceToken456");
        attendee.setUser(newUser);
        assertEquals(newUser, attendee.getUser());

        // Update event ID
        attendee.setEventID("event456");
        assertEquals("event456", attendee.getEventID());

        // Update RSVP status
        attendee.setRsvp(false);
        assertFalse("RSVP should be updated to false", attendee.isRsvp());

        // Update checked-in status
        attendee.setCheckedIn(true);
        assertTrue("CheckedIn should be updated to true", attendee.isCheckedIn());

        // Update check-in count
        attendee.setCheckinCount(1L);
        assertEquals(Long.valueOf(1), attendee.getCheckinCount());

        attendee.setLocation("New Location");
        assertEquals("New Location", attendee.getLocation());
    }
}
