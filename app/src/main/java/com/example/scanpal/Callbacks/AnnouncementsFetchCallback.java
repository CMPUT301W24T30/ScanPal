package com.example.scanpal.Callbacks;

import com.example.scanpal.Models.Announcement;

import java.util.List;

public interface AnnouncementsFetchCallback {
    void onSuccess(List<Announcement> notifications);

    void onError(Exception e);
}
