package com.example.scanpal.Callbacks;

/**
 * An interface defining callbacks for user sign-up operations.
 */
public interface UserSignedUpCallback {

    /**
     * Called when the user sign-up operation is completed.
     * @param isSignedUp A boolean indicating whether the user signed up successfully.
     */
    void onResult(boolean isSignedUp);

    /**
     * Called when an error occurs during the user sign-up operation.
     * @param e The exception indicating the error encountered.
     */
    void onError(Exception e);
}
