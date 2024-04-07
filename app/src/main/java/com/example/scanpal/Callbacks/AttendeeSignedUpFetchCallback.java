package com.example.scanpal.Callbacks;

import com.example.scanpal.Models.Attendee;

import java.util.ArrayList;

/**
 * Callback interface for signed-up attendees fetch operations.
 */
public interface AttendeeSignedUpFetchCallback {

    /**
     * Called when the attendee fetch operation is successful.
     */
    void onSuccess(ArrayList<Attendee> attendees);


    /**
     * Called when an error occurs during the attendee fetch operation.
     *
     * @param e The exception indicating the error encountered.
     */
    void onFailure(Exception e);
}