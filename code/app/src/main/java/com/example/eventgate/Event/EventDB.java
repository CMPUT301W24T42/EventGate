package com.example.eventgate.Event;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This is used to add, remove, and retrieve event data from the database
 */
public class EventDB {
    /**
     * An instance of the Firebase Firestore database
     */
    private final FirebaseFirestore db;
    /**
     * The collection for the events collection in the database
     */
    private final CollectionReference collection;
    /**
     * The TAG for logging
     */
    final String TAG = "EventDB";

    /**
     * Constructs a new EventDB
     */
    public EventDB() {
        db = FirebaseFirestore.getInstance();
        collection = db.collection("events");
    }

    /**
     * Adds an event to the firebase database
     * @param event the event to add
     */
    public void AddEvent(Event event) {
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
     * Adds an organizer event to the database.
     *
     * @param event         The event object containing details of the event.
     * @param eventQRBitmap The bitmap image of the event's QR code.
     */
    public void AddOrganizerEvent(Event event, Bitmap eventQRBitmap) {
        // Convert bitmap to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        eventQRBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] byteArray = baos.toByteArray();

        // Convert byte array to list of integers
        List<Integer> byteArrayAsList = new ArrayList<>();
        for (byte b : byteArray) {
            byteArrayAsList.add((int) b);
        }

        String eventId = collection.document().getId();
        event.setEventId(eventId);

        HashMap<String, String> data = new HashMap<>();
        data.put("eventId", event.getEventId());
        data.put("name", event.getEventName());
        data.put("checkInQRCode", byteArrayAsList.toString());
        data.put("organizer", ""); // Set organizer field to blank
        data.put("attendees", ""); // Set attendees field to blank

        collection
                .document(eventId)
                .set(data)
                .addOnSuccessListener(unused -> Log.d(TAG, "Event has been added successfully!"))
                .addOnFailureListener(e -> Log.d(TAG, "Event could not be added!" + e));
    }
}
