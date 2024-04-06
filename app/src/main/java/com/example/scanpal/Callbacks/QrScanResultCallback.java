package com.example.scanpal.Callbacks;

public interface QrScanResultCallback {
    void onResult(String eventID);

    void onError(String errorMessage);
}
