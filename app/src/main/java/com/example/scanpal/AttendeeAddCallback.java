package com.example.scanpal;

public interface AttendeeAddCallback {
    void onSuccess(Attendee attendee);
    void onError(Exception e);
}
