package com.example.scanpal;

/**
 * Callback interface for event update operations.
 */
public interface EventUpdateCallback {
    void onSuccess(boolean status);

    void onError(Exception e);
}