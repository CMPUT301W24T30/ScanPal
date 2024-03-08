package com.example.scanpal;

/**
 * Callback interface for attendee fetch operations.
 */
public interface AttendeeFetchCallback {
    void onSuccess(Attendee attendee);

    void onError(Exception e);
}
