package com.example.scanpal;

import java.util.ArrayList;

/**
 * Callback interface for event fetch (by user) operations.
 */
public interface EventFetchByUserCallback {

    void onSuccess(ArrayList<Event> eventList);

    void onError(Exception e);
}
