package com.example.eventgate.event;

import android.util.Log;

import com.example.eventgate.MainActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

/**
 * This is used to add, remove, and retrieve event data from the database
 */
public class EventDB {
    /**
     * An instance of the Firebase Firestore database
     */
    private FirebaseFirestore db;
    /**
     * The collection for the events collection in the database
     */
    private CollectionReference collection;
    /**
     * The TAG for logging
     */
    final String TAG = "EventDB";

    /**
     * Constructs a new EventDB
     */
    public EventDB() {
        db = MainActivity.db.getDB();
        collection = MainActivity.db.getEventsRef();
    }

    /**
     * Adds an event to the firebase database
     * @param event the event to add
     */
    public void addEvent(Event event) {
        String eventId = collection.document().getId();
        event.setEventId(eventId);
        HashMap<String, String> data = new HashMap<>();
        data.put("eventId", event.getEventId());
        data.put("name", event.getEventName());
        collection
                .document(eventId)
                .set(data)
                .addOnSuccessListener(unused -> Log.d(TAG, "Event has been added successfully!"))
                .addOnFailureListener(e -> Log.d(TAG, "Event could not be added!" + e));
    }

    /**
     * Removes an event from the database
     * @param event the event to remove
     */
    public void removeEvent(Event event) {
        String eventId = event.getEventId();
        collection.document(eventId)
                .delete()
                .addOnSuccessListener(unused -> Log.d(TAG, "Event has been deleted successfully"))
                .addOnFailureListener(e -> Log.d(TAG, "Error deleting event" + e));
    }
}
