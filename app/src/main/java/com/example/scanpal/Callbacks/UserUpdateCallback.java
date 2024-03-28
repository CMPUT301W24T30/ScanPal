package com.example.scanpal.Callbacks;

/**
 * Callback interface for user update operations.
 */
public interface UserUpdateCallback {
    void onSuccess();

    void onError(Exception e);
}
