package com.example.scanpal.Callbacks;

/**
 * Callback interface for username check operations.
 */
public interface UsernameCheckCallback {
    void onUsernameTaken(boolean isTaken);

    void onError(Exception e);
}