package com.example.scanpal;

public interface AttendeeFetchCallback {
    void onSuccess(Attendee attendee);
    void onError(Exception e);
}
