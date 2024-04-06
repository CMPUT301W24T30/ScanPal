package com.example.scanpal.Callbacks;

import com.example.scanpal.Models.Event;

import java.util.List;

public interface EventsFetchCallback {
    void onSuccess(List<Event> events); // Correctly expect a List<Event>

    void onError(Exception e);
}

