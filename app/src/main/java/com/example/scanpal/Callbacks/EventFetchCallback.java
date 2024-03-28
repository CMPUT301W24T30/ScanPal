package com.example.scanpal.Callbacks;

import com.example.scanpal.Models.Event;

/**
 * Callback interface for user fetch operations.
 */
public interface EventFetchCallback {

    void onSuccess(Event event);

    void onError(Exception e);
}
