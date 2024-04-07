package com.example.scanpal.Callbacks;
import com.example.scanpal.Models.Event;
import java.util.List;

/**
 * An interface defining callbacks for event fetch (by user) operations.
 */
public interface EventFetchByUserCallback {

    /**
     * Called when the event fetch operation by user is successful.
     *
     * @param events A list of events retrieved successfully.
     */
    void onSuccess(List<Event> events);

    /**
     * Called when an error occurs during the event fetch operation by user.
     *
     * @param e The exception indicating the error encountered.
     */
    void onError(Exception e);
}
