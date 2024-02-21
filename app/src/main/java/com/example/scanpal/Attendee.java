package com.example.scanpal;

/**
 * When a user joins an event, it creates an attendee instance for them. This will contain
 * information about that user's data for this specific event
 */
public class Attendee {

    private User user;
    private Event event;
    private boolean checkedIn = false;
    private String location; //Check in location of the user


    public Attendee(User user, Event event) {
        this.user = user;
        this.event = event;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
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
}
