package com.example.scanpal;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.ArrayList;

/**
 * Base class for events, contains all event data.
 */
public class Event {

    private String id; // will be generated using UUID
    private String name;
    private String description;
    private User organizer;
    private String location;
    private long maximumAttendees;
    private ArrayList<Attendee> participants;
    private String signUpAddress;
    private Uri posterURI;
    private String infoAddress;
    private Bitmap qrToEvent;
    private Bitmap qrToCheckIn;

    /**
     * Constructs an event with an organizer, name, and description.
     *
     * @param organizer   The user who organized the event.
     * @param name        The name of the event.
     * @param description The description of the event.
     */
    public Event(User organizer, String name, String description) {
        this.organizer = organizer;
        this.name = name;
        this.description = description;
        participants = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public ArrayList<Attendee> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<Attendee> participants) {
        this.participants = participants;
    }

    public String getSignUpAddress() {
        return signUpAddress;
    }

    public void setSignUpAddress(String signUpAddress) {
        this.signUpAddress = signUpAddress;
    }

    public String getInfoAddress() {
        return infoAddress;
    }

    public void setInfoAddress(String infoAddress) {
        this.infoAddress = infoAddress;
    }

    public Bitmap getQrToEvent() {
        return qrToEvent;
    }

    public void setQrToEvent(Bitmap qrToEvent) {
        this.qrToEvent = qrToEvent;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getMaximumAttendees() {
        return maximumAttendees;
    }

    public void setMaximumAttendees(long maximumAttendees) {
        this.maximumAttendees = maximumAttendees;
    }

    public Bitmap getQrToCheckIn() {
        return qrToCheckIn;
    }

    public void setQrToCheckIn(Bitmap qrToCheckIn) {
        this.qrToCheckIn = qrToCheckIn;
    }

    public void setPosterURI(Uri posterURI) {
        this.posterURI = posterURI;
    }

    public Uri getPosterURI() {
        return posterURI;
    }

}