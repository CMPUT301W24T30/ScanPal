package com.example.scanpal;

import java.util.List;

public interface AnnouncementsFetchCallback {
    void onSuccess(List<Announcement> notifications);

    void onError(Exception e);
}
