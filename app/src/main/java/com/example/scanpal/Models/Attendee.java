package com.example.scanpal.Models;

/**
 * Represents an attendee of an event. This class encapsulates the data related to a user's
 * participation in an event, including their RSVP status and whether they have checked in.
 * Instances of this class are used to manage and track event attendees' interactions.
 */
public class Attendee {
    private String id;
    private User user;
    private String eventID;
    private boolean checkedIn = false;
    private boolean rsvp = false;
    private String location; //Check in location of the user

    private long checkinCount;

    public Attendee() {
    }

    public Attendee(User user, String eventID, boolean rsvp, boolean checkedIn, long checkinCount) {
        this.user = user;
        this.eventID = eventID;
        this.rsvp = rsvp;
        this.checkedIn = checkedIn;
        this.checkinCount = checkinCount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isRsvp() {
        return rsvp;
    }

    public void setRsvp(boolean rsvp) {
        this.rsvp = rsvp;
    }

    public long getCheckinCount() {
        return this.checkinCount;
    }

    public void setCheckinCount(long checkinCount) {
        this.checkinCount = checkinCount;
    }
}
