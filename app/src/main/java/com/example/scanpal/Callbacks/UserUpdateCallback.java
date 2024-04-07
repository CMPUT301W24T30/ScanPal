package com.example.scanpal.Callbacks;

/**
 * An interface defining callbacks for user update operations.
 */
public interface UserUpdateCallback {

    /**
     * Called when the user update operation is successful.
     */
    void onSuccess();

    /**
     * Called when an error occurs during the user update operation.
     * @param e The exception indicating the error encountered.
     */
    void onError(Exception e);
}
