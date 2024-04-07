package com.example.scanpal.Callbacks;

/**
 * An interface defining callbacks for username check operations.
 */
public interface UsernameCheckCallback {

    /**
     * Called when the username check operation indicates whether the username is taken or not.
     *
     * @param isTaken A boolean indicating if the username is already taken.
     */
    void onUsernameTaken(boolean isTaken);

    /**
     * Called when an error occurs during the username check operation.
     *
     * @param e The exception indicating the error encountered.
     */
    void onError(Exception e);
}
