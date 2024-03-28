package com.example.scanpal.Callbacks;

import com.example.scanpal.Models.User;

/**
 * Callback interface for user fetch operations.
 */
public interface UserFetchCallback {
    void onSuccess(User user);

    void onError(Exception e);
}
