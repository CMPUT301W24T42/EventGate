package com.example.eventgate.event;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.example.eventgate.MainActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
     * @param deviceId      The organizer's firebase installation id
     */
    public Bitmap AddOrganizerEvent(Event event, String deviceId) {
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

        HashMap<String, Object> data = new HashMap<>();
        data.put("eventId", event.getEventId());
        data.put("name", event.getEventName());
        data.put("checkInQRCode", byteArrayAsList.toString());
        data.put("organizer", deviceId); // Set organizer field to firebase installation id
        data.put("attendees", new ArrayList<String>()); // Set attendees field to blank
        data.put("eventDetails", event.getEventDetails());
        System.out.println(event.getEventDetails());
        System.out.println(event.getEventId());

        collection
                .document(eventId)
                .set(data)
                .addOnSuccessListener(unused -> Log.d(TAG, "Event has been added successfully!"))
                .addOnFailureListener(e -> Log.d(TAG, "Event could not be added!" + e));

        return eventQRBitmap;
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
                        attendeeEvents.add(eventId);
                        updates.put("events", attendeeEvents);
                        db.collection("attendees").document(attendee.getId()).update(updates);
                        // Add attendee to event collection
                        updates = new HashMap<>();
                        ArrayList<String> eventAttendees = (ArrayList<String>) documentSnapshot.get("attendees");
                        eventAttendees.add(attendee.getId());
                        updates.put("attendees", eventAttendees);
                        db.collection("events").document(eventId).update(updates);
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
        Log.d("ID", deviceId);
        CompletableFuture<ArrayList<Event>> futureEvents = new CompletableFuture<>();
        ArrayList<Event> events = new ArrayList<>();
        db.collection("attendees").whereEqualTo("deviceId", deviceId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {  // If there is no matching deviceId, simply return
                return;
            }
            DocumentSnapshot attendee = queryDocumentSnapshots.getDocuments().get(0);
            ArrayList<String> attendeeEvents = (ArrayList<String>) attendee.get("events");

            if (attendeeEvents.size() == 0) {  // If it's empty, simply return
                return;
            }
            attendeeEvents.removeIf(String::isEmpty);
            db.collection("events").whereIn(FieldPath.documentId(), attendeeEvents).get().addOnSuccessListener(queryResults -> {
                for (QueryDocumentSnapshot queryResult: queryResults) {
                    String eventName = queryResult.getString("name");
                    Event event = new Event(eventName);
                    event.setEventId(queryResult.getId());
                    events.add(event);
                }
                futureEvents.complete(events);
            });
        });
        return futureEvents;
    }

    /**
     * retrieves event details
     * @param eventID
     * @return event details
     */
    public CompletableFuture<String> getEventDetailsDB(String eventID) {
        CompletableFuture<String> futureEventDetails = new CompletableFuture<>();
        db.collection("events").document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        futureEventDetails.complete(null);
                    } else {
                        String eventDetails = documentSnapshot.getString("eventDetails");
                        futureEventDetails.complete(eventDetails);
                    }
                })
                .addOnFailureListener(e -> futureEventDetails.completeExceptionally(e));

        return futureEventDetails;
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

    /**
     * Returns a reference to the Events collection
     * @return the Events collection
     */
    public CollectionReference getCollection() {
        return collection;
    }

    /**
     * Get a list of events that an organizer has created given their firebase installation id
     * @param deviceId organizer's firebase installation id
     * @return CompleteableFuture of Arraylist of Events
     * */
    public CompletableFuture<ArrayList<Event>> getOrganizerEvents(String deviceId) {
        CompletableFuture<ArrayList<Event>> futureEvents = new CompletableFuture<>();
        ArrayList<Event> events = new ArrayList<>();
        db.collection("events").whereEqualTo("organizer", deviceId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {  // If there is no matching deviceId, simply return
                        return;
                    }
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event newEvent = new Event(doc.getString("name"));
                        newEvent.setEventId(doc.getId());
                        events.add(newEvent);
                    }
                    futureEvents.complete(events);
                });
        return futureEvents;
    }

    //finds all attendees of an event
    public CompletableFuture<List<String>> getAttendeesForEvent(String eventId) {
        CompletableFuture<List<String>> allAttendees = new CompletableFuture<>();

        collection.document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> attendees = (List<String>) documentSnapshot.get("attendees");
                if (attendees != null) {
                    allAttendees.complete(attendees);
                } else {
                    // if empty
                    allAttendees.complete(new ArrayList<>());
                }
            } else {
                Log.d(TAG, "Missing event with id: " + eventId);
                allAttendees.completeExceptionally(new Exception("No such document"));
            }
        }).addOnFailureListener(e -> {
            Log.d(TAG, "Error getting document: ", e);
            allAttendees.completeExceptionally(e);
        });

        return allAttendees;
    }


}