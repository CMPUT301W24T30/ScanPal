package com.example.scanpal.Callbacks;
import com.example.scanpal.Models.Event;

/**
 * An interface defining callbacks for user fetch operations.
 */
public interface EventFetchCallback {

    /**
     * Called when the user fetch operation is successful.
     *
     * @param event The event retrieved successfully.
     */
    void onSuccess(Event event);

    /**
     * Called when an error occurs during the user fetch operation.
     *
     * @param e The exception indicating the error encountered.
     */
    void onError(Exception e);
}
