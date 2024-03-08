package com.example.scanpal;

import java.io.Serializable;

/**
 * When a user joins an event, it creates an attendee instance for them. This will contain
 * information about that user's data for this specific event
 */
public class Attendee implements Serializable {
    private static final long serialVersionUID = 1L;

    /*
    ID is a combo of username + eventID
     */
    private String id;
    private User user;
    private String eventID;
    private boolean checkedIn = false;
    private boolean rsvp = false;
    private String location; //Check in location of the user

    public Attendee() {
    }

    public Attendee(User user, String eventID, boolean rsvp, boolean checkedIn) {
        this.user = user;
        this.eventID = eventID;
        this.rsvp = rsvp;
        this.checkedIn = checkedIn;
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

    public void setEvent(String eventID) {
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
}
