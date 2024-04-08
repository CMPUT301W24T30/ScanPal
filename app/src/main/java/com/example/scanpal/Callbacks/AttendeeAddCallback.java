package com.example.scanpal.Callbacks;

/**
 * Callback interface for attendee add operations.
 */
public interface AttendeeAddCallback {

    /**
     * Called when the attendee add operation is successful.
     */
    void onSuccess();


    /**
     * Called when an error occurs during the attendee add operation.
     *
     * @param e The exception indicating the error encountered.
     */
    void onError(Exception e);
}
