package com.example.scanpal.Callbacks;

import java.util.List;

public interface EventIDsFetchCallback {
    void onSuccess(List<String> EventIDs);

    void onError(Exception e);
}
