package com.example.scanpal.Callbacks;
import java.util.List;

/**
 * An interface defining callbacks for event IDs fetch operations.
 */
public interface EventIDsFetchCallback {

    /**
     * Called when the event IDs fetch operation is successful.
     *
     * @param eventIDs A list of event IDs retrieved successfully.
     */
    void onSuccess(List<String> eventIDs);

    /**
     * Called when an error occurs during the event IDs fetch operation.
     *
     * @param e The exception indicating the error encountered.
     */
    void onError(Exception e);
}
