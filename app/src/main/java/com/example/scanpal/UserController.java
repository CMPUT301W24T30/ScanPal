package com.example.scanpal;

import com.google.firebase.firestore.FirebaseFirestore;

public class UserController {
    private FirebaseFirestore database; //instance of the database


    public void setDatabase(FirebaseFirestore database) {
        this.database = database;
    }

    public FirebaseFirestore getDatabase() {
        return this.database;
    }

    public void addUser() {// will be responsible for add all user types to the database

    }
}
