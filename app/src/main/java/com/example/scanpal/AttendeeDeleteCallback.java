package com.example.scanpal;

/**
 * Interface definition for a callback to be invoked when an attendee is successfully deleted or an error occurs.
 */
public interface AttendeeDeleteCallback {
    void onSuccess();

    void onError(Exception e);
}