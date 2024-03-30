package com.example.scanpal.Callbacks;

/**
 * Callback interface for attendee add operations.
 */
public interface AttendeeAddCallback {
    void onSuccess();

    void onError(Exception e);
}
