package com.example.scanpal;

import java.util.ArrayList;

/**
 * Callback interface for signed-up attendees fetch operations.
 */
public interface AttendeeSignedUpFetchCallback {
    void onSuccess(ArrayList<Attendee> attendees);

    void onFailure(Exception e);
}