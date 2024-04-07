package com.example.scanpal.Callbacks;

import com.example.scanpal.Models.Attendee;

/**
 * Callback interface for attendee fetch operations.
 */
public interface
AttendeeFetchCallback {

    /**
     * Called when the attendee fetch operation is successful.
     */
    void onSuccess(Attendee attendee);

    /**
     * Called when an error occurs during the attendee fetch operation.
     *
     * @param e The exception indicating the error encountered.
     */
    void onError(Exception e);
}
