package com.example.scanpal;

public class Attendee extends User {

    Attendee(String newName) {
        super(newName);
        super.adminFlag = false;
    }

}
