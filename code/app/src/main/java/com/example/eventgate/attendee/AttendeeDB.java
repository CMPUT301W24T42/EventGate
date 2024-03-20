package com.example.eventgate.attendee;

import com.example.eventgate.MainActivity;
import com.example.eventgate.event.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * This is used to add, remove, and retrieve attendee data from the database
 */
public class AttendeeDB {
    /**
     * An instance of the Firebase Firestore database
     */
    private FirebaseFirestore db;
    /**
     * The collection for the attendees collection in the database
     */
    private CollectionReference collection;
    /**
     * The TAG for logging
     */
    final String TAG = "AttendeeDB";

    /**
     * Constructs a new AttendeeDB
     */
    public AttendeeDB() {
        db = MainActivity.db.getDB();
        collection = MainActivity.db.getAttendeesRef();
    }

    /**
     * removes the event from attendees list of events in the case that the event is deleted
     * @param attendees a list of attendees who are attending the event that is being removed
     * @param eventId the id of the event to be removed
     */
    public void removeEvent(ArrayList<String> attendees, String eventId) {
        for (String attendee : attendees) {
            collection
                    .document(attendee)
                    .update("events", FieldValue.arrayRemove(eventId));
        }
    }
}
