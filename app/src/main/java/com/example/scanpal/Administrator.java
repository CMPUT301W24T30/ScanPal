package com.example.scanpal;

public class Administrator extends User {

    Administrator(String username) {
        super(username);
        super.adminFlag = true; //set this account to admin

        // stuff to add new admin to the database
    }


}
