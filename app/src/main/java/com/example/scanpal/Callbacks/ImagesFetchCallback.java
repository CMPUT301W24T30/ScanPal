package com.example.scanpal.Callbacks;

import java.util.List;

public interface ImagesFetchCallback {

    void onSuccess(List<String> images);

    void onError(Exception e);

}
