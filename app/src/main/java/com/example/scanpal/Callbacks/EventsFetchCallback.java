package com.example.scanpal.Callbacks;
import com.example.scanpal.Models.Event;
import java.util.List;

/**
 * An interface defining callbacks for events fetch operations.
 */
public interface EventsFetchCallback {

    /**
     * Called when the events fetch operation is successful.
     *
     * @param events A list of events retrieved successfully.
     */
    void onSuccess(List<Event> events);

    /**
     * Called when an error occurs during the events fetch operation.
     *
     * @param e The exception indicating the error encountered.
     */
    void onError(Exception e);
}

