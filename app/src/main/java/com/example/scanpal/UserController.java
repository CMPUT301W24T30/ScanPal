package com.example.scanpal;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserController {
    private final FirebaseFirestore database; //instance of the database

    public UserController(FirebaseFirestore database) {
        this.database = database;
    }

    public FirebaseFirestore getDatabase() {
        return this.database;
    }

    //TODO Check if User ID Already exists
    public void addUser(User user) {
        // will be responsible for add all user types to the database
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("administrator", user.isAdministrator());
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());

        DocumentReference docRef = database.collection("Users").document(user.getUsername());

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    System.out.println("User already exists!");
                } else {
                    docRef.set(userMap)
                            .addOnSuccessListener(aVoid -> System.out.println("User added successfully!"))
                            .addOnFailureListener(e -> System.out.println("Error adding user: " + e.getMessage()));
                }
            } else {
                System.out.println("Error checking user existence: " + task.getException());
            }
        });
    }
}
