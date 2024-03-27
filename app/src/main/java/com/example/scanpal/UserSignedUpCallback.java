package com.example.scanpal;

public interface UserSignedUpCallback {
    void onResult(boolean isSignedUp);

    void onError(Exception e);
}
