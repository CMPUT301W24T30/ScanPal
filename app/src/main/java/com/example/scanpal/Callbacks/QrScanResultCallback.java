package com.example.scanpal.Callbacks;

/**
 * An interface defining callbacks for QR scan result operations.
 */
public interface QrScanResultCallback {

    /**
     * Called when the QR scan operation results in a valid event ID.
     *
     * @param eventID The ID of the event scanned from the QR code.
     */
    void onResult(String eventID);

    /**
     * Called when an error occurs during the QR scan operation.
     *
     * @param errorMessage The error message indicating the encountered issue.
     */
    void onError(String errorMessage);
}
