package com.example.scanpal.Callbacks;
import com.example.scanpal.Models.User;

/**
 * An interface defining callbacks for user fetch operations.
 */
public interface UserFetchCallback {

    /**
     * Called when the user fetch operation is successful.
     *
     * @param user The user retrieved successfully.
     */
    void onSuccess(User user);

    /**
     * Called when an error occurs during the user fetch operation.
     *
     * @param e The exception indicating the error encountered.
     */
    void onError(Exception e);
}
