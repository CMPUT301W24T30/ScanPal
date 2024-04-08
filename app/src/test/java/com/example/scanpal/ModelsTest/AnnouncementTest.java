package com.example.scanpal.ModelsTest;

import com.example.scanpal.Models.Announcement;
import org.junit.Before;
import org.junit.Test;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import static org.junit.Assert.*;

public class AnnouncementTest {

    private Announcement announcement;

    @Before
    public void setUp() {
        announcement = new Announcement();
    }

    @Test
    public void constructor_initializesTimeStampCorrectly() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd h:mm a");
        String expectedDateTime = dateFormat.format(calendar.getTime());


        assertEquals("The timestamp should match the current time formatted as 'yyyy-MM-dd h:mm a'",
                expectedDateTime.substring(0, 10), // Check only date part to avoid minor time discrepancies
                announcement.getTimeStamp().substring(0, 10));
    }

    @Test
    public void settingAndGettingProperties_worksCorrectly() {
        announcement.setAnnouncementNum(5L);
        assertEquals("Announcement number should be set to 5", Long.valueOf(5), announcement.getAnnouncementNum());

        announcement.setEventID("Event123");
        assertEquals("Event ID should be 'Event123'", "Event123", announcement.getEventID());

        announcement.setMessage("This is a test announcement");
        assertEquals("Message should be 'This is a test announcement'", "This is a test announcement", announcement.getMessage());

    }
}
