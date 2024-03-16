package com.example.scanpal;

/**
 * Callback interface for delete all attendees (linked to user) operations.
 */
public interface DeleteAllAttendeesCallback {
    void onSuccess();

    void onError(Exception e);
}
