package com.example.scanpal;

/**
 * Callback interface for attendee update operations.
 */
public interface AttendeeUpdateCallback {
    void onSuccess();

    void onError(Exception e);
}
