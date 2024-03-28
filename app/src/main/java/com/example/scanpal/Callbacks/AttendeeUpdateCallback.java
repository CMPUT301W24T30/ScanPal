package com.example.scanpal.Callbacks;

/**
 * Callback interface for attendee update operations.
 */
public interface AttendeeUpdateCallback {
    void onSuccess();

    void onError(Exception e);
}
