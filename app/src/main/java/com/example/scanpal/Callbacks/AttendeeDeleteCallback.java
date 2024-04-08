package com.example.scanpal.Callbacks;

/**
 * Interface definition for a callback to be invoked when an attendee is successfully deleted or an error occurs.
 */
public interface AttendeeDeleteCallback {

    /**
     * Called when the attendee delete operation is successful.
     */
    void onSuccess();

    /**
     * Called when an error occurs during the attendee delete operation.
     *
     * @param e The exception indicating the error encountered.
     */
    void onError(Exception e);
}