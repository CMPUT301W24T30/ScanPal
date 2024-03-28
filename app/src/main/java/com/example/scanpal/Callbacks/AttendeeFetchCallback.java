package com.example.scanpal.Callbacks;

import com.example.scanpal.Models.Attendee;

/**
 * Callback interface for attendee fetch operations.
 */
public interface AttendeeFetchCallback {
    void onSuccess(Attendee attendee);

    void onError(Exception e);
}
