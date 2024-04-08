package com.example.scanpal.Callbacks;

/**
 * An interface defining callbacks for user add operations.
 */
public interface UserAddCallback {

    /**
     * Called when the user add operation is successful.
     */
    void onSuccess();

    /**
     * Called when an error occurs during the user add operation.
     *
     * @param e The exception indicating the error encountered.
     */
    void onError(Exception e);
}
