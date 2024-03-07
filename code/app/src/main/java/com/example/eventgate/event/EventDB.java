package com.example.eventgate.event;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.util.Log;

import com.example.eventgate.MainActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

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
    private Bitmap eventQRBitmap;

    /**
     * Constructs a new EventDB
     */
    public EventDB() {
        db = MainActivity.db.getDB();
        collection = MainActivity.db.getEventsRef();
    }

    /**
     * Adds an event to the firebase database
     *
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
     * Adds an organizer event to the database.
     *
     * @param event         The event object containing details of the event.
     */
    public Bitmap AddOrganizerEvent(Event event) {
        String eventId = collection.document().getId();
        event.setEventId(eventId);

        // Create Check in QR Code
        MultiFormatWriter writer = new MultiFormatWriter();

        try {
            BitMatrix matrix = writer.encode(eventId, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder encoder = new BarcodeEncoder();
            eventQRBitmap = encoder.createBitmap(matrix);

        } catch (WriterException e) {
            e.printStackTrace();
        }

        event.setEventQRBitmap(eventQRBitmap);

        // Convert bitmap to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        eventQRBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] byteArray = baos.toByteArray();

        // Convert byte array to list of integers
        List<Integer> byteArrayAsList = new ArrayList<>();
        for (byte b : byteArray) {
            byteArrayAsList.add((int) b);
        }

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

        return eventQRBitmap;
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