package com.example.scanpal;

/**
 * Callback interface for user remove operations.
 */
public interface UserRemoveCallback {
    void onSuccess();

    void onError(Exception e);
}

