package com.example.scanpal;

public interface UserFetchCallback {
    void onSuccess(User user);

    void onError(Exception e);
}
