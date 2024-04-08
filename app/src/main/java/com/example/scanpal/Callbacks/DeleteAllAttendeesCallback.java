package com.example.scanpal.Callbacks;

/**
 * Callback interface for delete all attendees (linked to user) operations.
 */
public interface DeleteAllAttendeesCallback {


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
