package com.example.scanpal;

/**
 * Callback interface for attendee add operations.
 */
public interface AttendeeAddCallback {
    void onSuccess(Attendee attendee);

    void onError(Exception e);
}
