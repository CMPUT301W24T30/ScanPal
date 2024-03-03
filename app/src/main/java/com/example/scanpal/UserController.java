package com.example.scanpal;

import android.content.Context;
import android.net.Uri;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles operations related to user management in a Firestore database.
 */
public class UserController {
    private final FirebaseFirestore database;
    private final Context context;

    /**
     * Constructs a UserController with a reference to a Firestore database.
     *
     * @param database The Firestore database instance used for user operations.
     */
    public UserController(FirebaseFirestore database, Context context) {
        this.database = database;
        this.context = context;
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
        // Serialize and store user locally
        try {
            FileOutputStream fos = context.openFileOutput("user.ser", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(user);
            oos.close();
            fos.close();
        } catch (Exception e) {
            callback.onError(e);
            return;
        }

        // Add to Firestore
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("administrator", user.isAdministrator());
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());
        userMap.put("photo", user.getPhoto());

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

    public void updateUser(User user, UserUpdateCallback callback) {
        try {
            FileOutputStream fos = context.openFileOutput("user.ser", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(user);
            oos.close();
            fos.close();
        } catch (Exception e) {
            callback.onError(e);
            return;
        }

        // Update Firestore
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());
        userMap.put("photo", user.getPhoto());

        DocumentReference docRef = database.collection("Users").document(user.getUsername());
        docRef.update(userMap)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }


    /**
     * Retrieves a user from the Firestore database based on the username.
     *
     * @param username The username of the user to fetch.
     * @param callback The callback to be invoked upon completion of the fetch
     *                 operation.
     */
    public void getUser(String username, UserFetchCallback callback) {
        // fetch user from local storage
        try {
            FileInputStream fis = context.openFileInput("user.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            User user = (User) ois.readObject();
            ois.close();
            fis.close();
            callback.onSuccess(user);
            return;
        } catch (Exception e) {
            // If local fetch fails, fetch from Firestore
        }

        // Fetch from Firestore
        DocumentReference docRef = database.collection("Users").document(fetchStoredUsername());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    Map<String, Object> data = document.getData();
                    if (data != null) {
                        User user = new User(username, (String) data.get("firstName"), (String) data.get("lastName"));
                        user.setPhoto(String.valueOf((Uri) data.get("photo")));
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

    // fetch username from internal storage
    public String fetchStoredUsername() {
        try {
            FileInputStream fis = context.openFileInput("user.ser");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String username = bufferedReader.readLine();
            fis.close();
            return username;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}