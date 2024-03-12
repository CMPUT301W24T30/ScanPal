package com.example.scanpal;

/**
 * Callback interface for user fetch operations.
 */
public interface EventFetchCallback {

    void onSuccess(Event event);

    void onError(Exception e);
}
