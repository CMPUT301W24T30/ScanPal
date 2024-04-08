package com.example.scanpal.Models;

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
    private Long maximumAttendees;
    private ArrayList<Attendee> participants;
    private String signUpAddress;
    private Uri posterURI;
    private String infoAddress;
    private Bitmap qrToEvent;
    private Bitmap qrToCheckIn;
    private Long announcementCount;
    private boolean isUserSignedUp;
    private boolean trackLocation = false;
    private String locationCoords; // "lat,lon" format
    private Long totalCheckInCount;
    private String date;
    private String time;

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
        this.totalCheckInCount = 0L;
    }

    public boolean isTrackLocation() {
        return trackLocation;
    }

    public void setTrackLocation(boolean trackLocation) {
        this.trackLocation = trackLocation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocationCoords() {
        return locationCoords;
    }

    public void setLocationCoords(String coord) {
        this.locationCoords = coord;
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

    public Long getMaximumAttendees() {
        return maximumAttendees;
    }

    public void setMaximumAttendees(Long maximumAttendees) {
        this.maximumAttendees = maximumAttendees;
    }

    public Bitmap getQrToCheckIn() {
        return qrToCheckIn;
    }

    public void setQrToCheckIn(Bitmap qrToCheckIn) {
        this.qrToCheckIn = qrToCheckIn;
    }

    public Uri getPosterURI() {
        return posterURI;
    }

    public void setPosterURI(Uri posterURI) {
        this.posterURI = posterURI;
    }

    public Long getAnnouncementCount() {
        return announcementCount;
    }

    public void setAnnouncementCount(Long announcementCount) {
        this.announcementCount = announcementCount;
    }

    public Long getTotalCheckInCount() {
        return totalCheckInCount;
    }

    public void setTotalCheckInCount(Long totalCheckInCount) {
        this.totalCheckInCount = totalCheckInCount;
    }

    public boolean isUserSignedUp() {
        return isUserSignedUp;
    }

    public void setUserSignedUp(boolean userSignedUp) {
        isUserSignedUp = userSignedUp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}