package com.example.scanpal.Callbacks;

import com.example.scanpal.Models.Announcement;

import java.util.List;

/**
 * An interface defining callbacks for fetching announcements.
 */
public interface AnnouncementsFetchCallback {

    /**
     * Called when the announcements fetching operation is successful.
     *
     * @param notifications A list of announcements retrieved successfully.
     */
    void onSuccess(List<Announcement> notifications);


    /**
     * Called when an error occurs during the announcements fetching operation.
     *
     * @param e The exception indicating the error encountered.
     */
    void onError(Exception e);
}
