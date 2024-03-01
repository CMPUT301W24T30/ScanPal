package com.example.scanpal;

/**
 * Callback interface for user add operations.
 */
public interface UserAddCallback {
    void onSuccess();

    void onError(Exception e);
}
