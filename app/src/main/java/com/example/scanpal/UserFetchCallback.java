package com.example.scanpal;

/**
 * Callback interface for user fetch operations.
 */
public interface UserFetchCallback {
    void onSuccess(User user);

    void onError(Exception e);
}
