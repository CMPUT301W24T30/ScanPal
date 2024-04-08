package com.example.scanpal.Callbacks;

/**
 * An interface defining callbacks for event update operations.
 */
public interface EventUpdateCallback {

    /**
     * Called when the event update operation is successful.
     *
     * @param status A boolean indicating the status of the update operation.
     */
    void onSuccess(boolean status);

    /**
     * Called when an error occurs during the event update operation.
     *
     * @param e The exception indicating the error encountered.
     */
    void onError(Exception e);
}
