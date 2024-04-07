package com.example.scanpal.Callbacks;

/**
 * Callback interface for attendee update operations.
 */
public interface AttendeeUpdateCallback {

    /**
     * Called when the attendee update operation is successful.
     */
    void onSuccess();


    /**
     * Called when an error occurs during the attendee update operation.
     *
     * @param e The exception indicating the error encountered.
     */
    void onError(Exception e);
}
