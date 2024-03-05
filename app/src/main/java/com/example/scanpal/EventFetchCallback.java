package com.example.scanpal;

public interface EventFetchCallback {

    void onSuccess(Event event);

    void onError(Exception e);
}
