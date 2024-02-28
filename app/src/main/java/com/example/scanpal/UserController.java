package com.example.scanpal;

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


        // Save to database
        database.collection("Users").document(user.getUsername()).set(userMap)
                .addOnSuccessListener(aVoid -> System.out.println("User added successfully!"))
                .addOnFailureListener(e -> System.out.println("Error adding user: " + e.getMessage()));
    }
}
