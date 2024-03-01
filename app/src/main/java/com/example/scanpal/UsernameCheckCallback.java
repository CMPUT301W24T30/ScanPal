package com.example.scanpal;

public interface UsernameCheckCallback {
    void onUsernameTaken(boolean isTaken);

    void onError(Exception e);
}