package com.example.eventgate.Event;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
     * Checks a user into an event
     * @param deviceId attendee's firebase installation id
     * @param eventId event's unique id
     * @return 0 if successful, 1 if event not found, 2 if already checked-in
     * */
    public CompletableFuture<Integer> checkInAttendee(String deviceId, String eventId) {
        CompletableFuture<Integer> futureResult = new CompletableFuture<>();
        db.collection("events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                db.collection("attendees").whereEqualTo("deviceId", deviceId).get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                    DocumentSnapshot attendee = queryDocumentSnapshots.getDocuments().get(0);
                    ArrayList<String> attendeeEvents = (ArrayList<String>) attendee.get("events");
                    boolean alreadyExists = false;
                    for (String event : attendeeEvents) {
                        if (event.equals(eventId)) {
                            alreadyExists = true;
                            futureResult.complete(2);
                            break;
                        }
                    }
                    if (!alreadyExists) {
                        // Add event to attendee collection
                        Map<String, Object> updates = new HashMap<>();
                        attendeeEvents.add(attendeeEvents.size() - 1, eventId);
                        updates.put("events", attendeeEvents);
                        db.collection("attendees").document(attendee.getId()).update(updates);
                        // Add attendee to event collection
                        updates = new HashMap<>();
                        ArrayList<String> eventAttendees = (ArrayList<String>) documentSnapshot.get("attendees");
                        eventAttendees.add(eventAttendees.size() - 1, attendee.getId());
                        updates.put("attendees", attendeeEvents);
                        db.collection("events").document(attendee.getId()).update(updates);
                        futureResult.complete(0);
                    }
                });
            } else {
                futureResult.complete(1);
            }
        });
        return futureResult;
    }

    /**
     * Get a list of events that a user is checked into given their firebase installation id
     * @param deviceId attendee's firebase installation id
     * @return CompleteableFuture of Arraylist of Events
     * */
    public CompletableFuture<ArrayList<Event>> getAttendeeEvents(String deviceId) {
        CompletableFuture<ArrayList<Event>> futureEvents = new CompletableFuture<>();
        ArrayList<Event> events = new ArrayList<>();
        db.collection("attendees").whereEqualTo("deviceId", deviceId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
            DocumentSnapshot attendee = queryDocumentSnapshots.getDocuments().get(0);
            ArrayList<String> attendeeEvents = (ArrayList<String>) attendee.get("events");
            for (String eventId : attendeeEvents.subList(0, attendeeEvents.size() - 1)) {
                db.collection("events").document(eventId.trim()).get().addOnSuccessListener(documentSnapshot -> {
                    events.add(0, new Event(documentSnapshot.getString("name")));
                    // Check if this is the last event, if so, complete
                    if (eventId.equals(attendeeEvents.get(attendeeEvents.size() - 2))) {
                        futureEvents.complete(events);
                    }
                });
            }
        });
        return futureEvents;
    }
}
