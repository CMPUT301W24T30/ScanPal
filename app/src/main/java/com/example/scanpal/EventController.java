package com.example.scanpal;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventController {

    private final FirebaseFirestore database; //instance of the database

    public EventController(FirebaseFirestore database) {
        this.database = database;
    }

    public FirebaseFirestore getDatabase() {
        return this.database;
    }

    public void addEvent(Event event) {
        // will be responsible for add all user types to the database
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("name", event.getName());
        eventMap.put("description", event.getDescription());

        DocumentReference organizerRef = database.collection("Users").document(event.getOrganizer().getUsername());
        eventMap.put("organizer", organizerRef);

        List<DocumentReference> participantRefs = new ArrayList<>();
        for (Attendee participant : event.getParticipants()) {
            DocumentReference participantRef = database.collection("Attendees").document(participant.getUser().getUsername());
            participantRefs.add(participantRef);
        }
        eventMap.put("participants", participantRefs);

        // Save to database
        database.collection("Events").document(event.getId()).set(eventMap)
                .addOnSuccessListener(aVoid -> System.out.println("Event added successfully!"))
                .addOnFailureListener(e -> System.out.println("Error adding event: " + e.getMessage()));
    }
}
