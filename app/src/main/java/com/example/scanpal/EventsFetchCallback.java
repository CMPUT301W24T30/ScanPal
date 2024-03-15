package com.example.scanpal;

import java.util.List;

public interface EventsFetchCallback {
    void onSuccess(List<Event> events); // Correctly expect a List<Event>
    void onError(Exception e);
}

