package com.example.scanpal.Callbacks;

/**
 * Callback interface for user add operations.
 */
public interface UserAddCallback {
    void onSuccess();

    void onError(Exception e);
}
