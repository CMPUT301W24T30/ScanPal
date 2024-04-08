package com.example.scanpal.Callbacks;
import com.example.scanpal.Models.User;
import java.util.List;

/**
 * An interface defining callbacks for users fetch operations.
 */
public interface UsersFetchCallback {

    /**
     * Called when the users fetch operation is successful.
     * @param users A list of users retrieved successfully.
     */
    void onSuccess(List<User> users);

    /**
     * Called when an error occurs during the users fetch operation.
     * @param e The exception indicating the error encountered.
     */
    void onError(Exception e);
}