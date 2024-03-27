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
 * Handles interactions with the Firestore database for user-related operations. This includes
 * adding, updating, fetching, and removing user data, as well as managing user sessions locally.
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
     * @param user     The User object to add.
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

        //user.setDeviceToken( FirebaseMessaging.getInstance().getToken().getResult() );

        // Prepare user data for Firestore
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("administrator", user.isAdministrator());
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());
        userMap.put("photo", user.getPhoto());
        userMap.put("deviceToken", user.getDeviceToken());

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
                        User user = new User(username, (String) data.get("firstName"), (String) data.get("lastName"), (String) data.get("deviceToken"));
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
     * Removes a user from the Firestore database and deletes the local copy.
     *
     * @param username The username of the user to remove.
     * @param callback The callback to report success or failure.
     */
    public void removeUser(String username, UserRemoveCallback callback) {
        // delete user locally
        try {
            context.deleteFile("user.ser");
        } catch (Exception e) {
            callback.onError(e);
            return;
        }

        // delete user from Firestore
        DocumentReference docRef = database.collection("Users").document(username);
        docRef.delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
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

    /**
     * Checks if a user is signed up for a specific event based on the user's username and the event ID.
     * Utilizes the AttendeeController to fetch attendee information and determine their RSVP status.
     *
     * @param username The username of the user whose sign-up status is to be checked.
     * @param eventID  The unique identifier of the event.
     * @param callback A UserSignedUpCallback instance to handle the result or error of the check.
     *                 The callback's onResult method is invoked with true if the user has RSVP'd
     *                 to the event, or false otherwise.
     */
    public void isUserSignedUp(String username, String eventID, UserSignedUpCallback callback) {
        String attendeeID = username + eventID;
        AttendeeController attendeeController = new AttendeeController(database);

        attendeeController.fetchAttendee(attendeeID, new AttendeeFetchCallback() {
            @Override
            public void onSuccess(Attendee attendee) {
                callback.onResult(attendee.isRsvp());
            }

            @Override
            public void onError(Exception e) {
                callback.onResult(false);
            }
        });
    }
}