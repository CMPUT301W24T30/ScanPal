package com.example.scanpal;

/**
 * Callback interface for user update operations.
 */
public interface UserUpdateCallback {
    void onSuccess();

    void onError(Exception e);
}
