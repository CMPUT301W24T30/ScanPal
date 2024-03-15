package com.example.scanpal;

import android.content.Context;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manages user data interactions with a Firestore database, including adding,
 * updating, and fetching user details.
 */
public class UserController {
    private final FirebaseFirestore database;
    private final Context context;

    /**
     * Initializes a new UserController instance.
     *
     * @param database The Firestore database instance used for user operations.
     * @param context  The application's context, used for file operations.
     */
    public UserController(FirebaseFirestore database, Context context) {
        this.database = database;
        this.context = context;
    }

    /**
     * Adds a new user to the Firestore database and stores a local copy. If the
     * username already exists in the database, an error is reported through the
     * callback.
     *
     * @param user     The User object to add to the database.
     * @param callback The callback to report success or failure.
     */
    public void addUser(User user, UserAddCallback callback) {
        // Attempt to serialize and store user locally
        try {
            FileOutputStream fos = context.openFileOutput("user.ser", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(user);
            oos.close();
            fos.close();
        } catch (Exception e) {
            callback.onError(e);
        }

        // Prepare user data for Firestore
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("administrator", user.isAdministrator());
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());
        userMap.put("photo", user.getPhoto());

        // Attempt to add user to Firestore
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
     * Updates an existing user's details in the Firestore database and updates
     * the local copy.
     *
     * @param user     The User object with updated details.
     * @param callback The callback to report success or failure.
     */
    public void updateUser(User user, UserUpdateCallback callback) {
        // Attempt to serialize and update user locally
        try {
            FileOutputStream fos = context.openFileOutput("user.ser", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(user);
            oos.close();
            fos.close();
        } catch (Exception e) {
            callback.onError(e);
        }

        // Prepare updated user data for Firestore
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());
        userMap.put("photo", user.getPhoto());

        // Attempt to update user in Firestore
        DocumentReference docRef = database.collection("Users").document(user.getUsername());
        docRef.update(userMap)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Fetches a user's details from the Firestore database. If the user is not
     * found in Firestore, attempts to retrieve a local copy.
     *
     * @param username The username of the user to fetch.
     * @param callback The callback to report the fetched user or failure.
     */
    public void getUser(String username, UserFetchCallback callback) {
        // Attempt to fetch user from local storage
        try {
            FileInputStream fis = context.openFileInput("user.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            User user = (User) ois.readObject();
            ois.close();
            fis.close();
            callback.onSuccess(user);
            return;
        } catch (Exception ignored) {
        }

        // Fetch user from Firestore
        DocumentReference docRef = database.collection("Users").document(Objects.requireNonNull(fetchStoredUsername()));
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    Map<String, Object> data = document.getData();
                    if (data != null) {
                        User user = new User(username, (String) data.get("firstName"), (String) data.get("lastName"));
                        user.setPhoto(String.valueOf(data.get("photo")));
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
     * Checks if the provided username already exists in the Firestore database.
     *
     * @param username The username to check.
     * @param callback The callback to report the result of the check.
     */
    public void isUsernameTaken(String username, UsernameCheckCallback callback) {
        DocumentReference docRef = database.collection("Users").document(username);
        docRef.get().addOnSuccessListener(task -> callback.onUsernameTaken(task != null && task.exists())).addOnFailureListener(callback::onError);
    }

    /**
     * Retrieves the stored username from internal storage.
     *
     * @return The stored username, or null if an error occurs.
     */
    public String fetchStoredUsername() {
        try {
            FileInputStream fis = context.openFileInput("user.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            User user = (User) ois.readObject();
            fis.close();
            return user.getUsername();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check internal storage to see if user object exists.
     *
     * @return true if user object exists in internal storage, false if not.
     */
    public boolean isUserLoggedIn() {
        String filename = "user.ser";
        FileInputStream fis;
        try {
            fis = context.openFileInput(filename);
            if (fis != null) {
                fis.close();
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

}