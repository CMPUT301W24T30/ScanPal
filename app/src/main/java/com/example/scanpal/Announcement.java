package com.example.scanpal;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Announcement {

    /**
     * The # this announcement is for its specific event,
     * for example if event X has made 4 announcements
     * are you are creating this one, a new one, then
     * it would be announcement number 5
     */
    private Long announcementNum;
    private String eventID;
    private String message;
    private String timeStamp;

    Announcement() {
        Calendar calendar = Calendar.getInstance();

        // Format the date and time with AM/PM
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd h:mm a");
        String formattedDateTime = dateFormat.format(calendar.getTime());

        timeStamp =  formattedDateTime;//Calendar.getInstance().getTime().toString();
    }

    public Long getAnnouncementNum() {
        return announcementNum;
    }

    public void setAnnouncementNum(Long announcementNum) {
        this.announcementNum = announcementNum;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
