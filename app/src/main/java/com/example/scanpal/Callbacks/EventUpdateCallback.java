package com.example.scanpal.Callbacks;

/**
 * Callback interface for event update operations.
 */
public interface EventUpdateCallback {
    void onSuccess(boolean status);

    void onError(Exception e);
}