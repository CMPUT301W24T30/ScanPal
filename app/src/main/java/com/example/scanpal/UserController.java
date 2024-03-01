package com.example.scanpal;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles operations related to user management in a Firestore database.
 */
public class UserController {
    private final FirebaseFirestore database;

    /**
     * Constructs a UserController with a reference to a Firestore database.
     *
     * @param database The Firestore database instance used for user operations.
     */
    public UserController(FirebaseFirestore database) {
        this.database = database;
    }

    /**
     * Adds a new user to the Firestore database if the username does not already
     * exist.
     *
     * @param user     The user to be added to the database.
     * @param callback The callback to be invoked upon completion of the add
     *                 operation.
     */
    public void addUser(User user, UserAddCallback callback) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("administrator", user.isAdministrator());
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());

        DocumentReference docRef = database.collection("Users").document(user.getUsername());

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    callback.onError(new Exception("User already exists!"));
                } else {
                    docRef.set(userMap)
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(callback::onError);
                }
            } else {
                callback.onError(new Exception("Error checking user existence", task.getException()));
            }
        });
    }

    /**
     * Retrieves a user from the Firestore database based on the username.
     *
     * @param username The username of the user to fetch.
     * @param callback The callback to be invoked upon completion of the fetch
     *                 operation.
     */
    public void getUser(String username, UserFetchCallback callback) {
        DocumentReference docRef = database.collection("Users").document(username);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    Map<String, Object> data = document.getData();
                    if (data != null) {
                        User user = new User(username, (String) data.get("firstName"), (String) data.get("lastName"));
                        callback.onSuccess(user);
                    } else {
                        callback.onError(new Exception("Failed to parse user data"));
                    }
                } else {
                    callback.onError(new Exception("User does not exist"));
                }
            } else {
                callback.onError(new Exception("Error fetching user", task.getException()));
            }
        });
    }

    /**
     * Checks if a username is already taken in the Firestore database.
     *
     * @param username The username to check for availability.
     * @param callback The callback to be invoked with the result of the check.
     */
    public void isUsernameTaken(String username, UsernameCheckCallback callback) {
        DocumentReference docRef = database.collection("Users").document(username);
        docRef.get().addOnSuccessListener(task -> {
            System.out.println(task);
            callback.onUsernameTaken(task != null && task.exists());
        }).addOnFailureListener(callback::onError);

    }

}
