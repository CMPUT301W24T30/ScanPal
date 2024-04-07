package com.example.scanpal.Callbacks;

/**
 * An interface defining callbacks for user remove operations.
 */
public interface UserRemoveCallback {

    /**
     * Called when the user remove operation is successful.
     */
    void onSuccess();

    /**
     * Called when an error occurs during the user remove operation.
     *
     * @param e The exception indicating the error encountered.
     */
    void onError(Exception e);
}
