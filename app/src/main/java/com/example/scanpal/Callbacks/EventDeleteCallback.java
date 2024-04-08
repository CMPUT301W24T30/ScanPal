package com.example.scanpal.Callbacks;

/**
 * An interface defining callbacks for event deletion operations.
 */
public interface EventDeleteCallback {

    /**
     * Called when the event deletion operation is successful.
     */
    void onSuccess();

    /**
     * Called when an error occurs during the event deletion operation.
     *
     * @param e The exception indicating the error encountered.
     */
    void onError(Exception e);
}
