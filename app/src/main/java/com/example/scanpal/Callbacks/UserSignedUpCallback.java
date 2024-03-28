package com.example.scanpal.Callbacks;

public interface UserSignedUpCallback {
    void onResult(boolean isSignedUp);

    void onError(Exception e);
}
