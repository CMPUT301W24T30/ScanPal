package com.example.scanpal;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AttendeeController {
    private final FirebaseFirestore database; //instance of the database

    public AttendeeController(FirebaseFirestore database) {
        this.database = database;
    }

    public FirebaseFirestore getDatabase() {
        return this.database;
    }

    public void addAttendee(Attendee attendee) {
        // will be responsible for add all user types to the database
        Map<String, Object> attendeeMap = new HashMap<>();
        attendeeMap.put("location", attendee.getLocation());

        attendeeMap.put("checkedIn", attendee.isCheckedIn());

        DocumentReference userRef = database.collection("Users").document(attendee.getUser().getUsername());
        attendeeMap.put("user", userRef);

        DocumentReference eventRef = database.collection("Events").document(attendee.getEvent().getId());
        attendeeMap.put("event", eventRef);

        // Save to database
        database.collection("Attendees").document(attendee.getId()).set(attendeeMap)
                .addOnSuccessListener(aVoid -> System.out.println("Attendee added successfully!"))
                .addOnFailureListener(e -> System.out.println("Error adding attendee: " + e.getMessage()));
    }
}
