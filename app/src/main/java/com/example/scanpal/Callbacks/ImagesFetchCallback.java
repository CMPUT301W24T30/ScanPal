package com.example.scanpal.Callbacks;

import android.net.Uri;

import com.example.scanpal.Models.ImageData;
import com.example.scanpal.Models.User;

import java.util.List;

public interface ImagesFetchCallback {

        void onSuccess(List<String> images);
        void onError(Exception e);

}
