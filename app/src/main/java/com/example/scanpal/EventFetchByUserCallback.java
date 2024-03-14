package com.example.scanpal;

import java.util.List;

/**
 * Callback interface for event fetch (by user) operations.
 */
public interface EventFetchByUserCallback {
    void onSuccess(List<Event> events); // Correctly expect a List<Event>
    void onError(Exception e);
}