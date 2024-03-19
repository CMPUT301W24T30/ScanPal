package com.example.scanpal;

/**
 * Callback interface for attendee add operations.
 */
public interface AttendeeAddCallback {
    void onSuccess();

    void onError(Exception e);
}
