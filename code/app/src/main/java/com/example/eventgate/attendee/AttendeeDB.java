package com.example.eventgate.attendee;

import android.content.SharedPreferences;
import android.util.Log;

import com.example.eventgate.MainActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is used to add, remove, and retrieve attendee data from the database
 */
public class AttendeeDB {
    /**
     * The collection for the attendees collection in the database
     */
    private final CollectionReference collection;
    /**
     * The TAG for logging
     */
    final String TAG = "AttendeeDB";

    /**
     * Constructs a new AttendeeDB
     */
    public AttendeeDB() {
        collection = MainActivity.db.getAttendeesRef();
    }

    /**
     * this creates a new attendee profile and stores the info in the attendees collection in the database
     * @param attendeesRef a reference to the attendees collection
     * @param deviceId the firebase installation id of the current user
     */
    public void createNewAttendee(CollectionReference attendeesRef, String deviceId, SharedPreferences preferences) {
        String attendeeId = attendeesRef.document().getId();
        HashMap<String, Object> data = new HashMap<>();
        data.put("deviceId", deviceId);
        // set an attendee's name to their id by default until the user enters it later in user settings
        data.put("name", attendeeId);
        data.put("uUid", MainActivity.db.getUser().getUid());
        data.put("events", new ArrayList<Integer>());
        data.put("trackingEnabled", false);
        attendeesRef.document(attendeeId).set(data)
                .addOnSuccessListener(unused -> {
                    // store info in shared preferences
                    preferences.edit()
                            .putString("attendeeName", attendeeId)
                            .putString("attendeeId", attendeeId)
                            .apply();
                    Log.d("Firebase Firestore", "Attendee has been added successfully!");
                })
                .addOnFailureListener(e -> Log.d("Firebase Firestore", "Attendee could not be added!" + e));
    }

    /**
     * this creates a new attendee profile and stores the info in the attendees collection in the database
     * @param attendeesRef a reference to the attendees collection
     * @param deviceId the firebase installation id of the current user
     */
    public void editAttendee(CollectionReference attendeesRef, String deviceId, String name, String homepage, Boolean tracking) {
        attendeesRef.whereEqualTo("deviceId", deviceId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
            DocumentSnapshot attendee = queryDocumentSnapshots.getDocuments().get(0);
            HashMap<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("homepage", homepage);
            data.put("trackingEnabled", tracking);
            attendeesRef.document(attendee.getId()).update(data)
                    .addOnSuccessListener(unused -> {
                        Log.d("Firebase Firestore", "Attendee edited successfully!");
                    })
                    .addOnFailureListener(e -> Log.d("Firebase Firestore", "Attendee could not be added!" + e));
        });
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

    /**
     * removes an attendee from all the events they are attending in the case that the attendee/user
     *      is removed by an admin
     * @param attendee the attendee to be removed
     */
    public void removeAttendee(Attendee attendee) {
        String attendeeId = attendee.getAttendeeId();
        // get references to the attendee's document and the events collection in the database
        DocumentReference attendeeRef = collection.document(attendeeId);
        CollectionReference eventsRef = MainActivity.db.getEventsRef();

        attendeeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // get the list of events that the attendee is attending
                    ArrayList<String> eventsList = (ArrayList<String>) document.get("events");
                    // go through events and remove the attendee from the event's list of attendees
                    //      and remove the event from the attendee's list of events
                    for (String eventId : eventsList) {
                        eventsRef.document(eventId).update("attendees", FieldValue.arrayRemove(attendeeId));
                        attendeeRef.update("events", FieldValue.arrayRemove(eventId));
                    }
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    /**
     * this removes an attendee from the specified event (used by admin)
     * @param attendee the attendee that will be removed
     * @param eventId the event that the attendee will be removed from
     */
    public void removeAttendeeFromEvent(Attendee attendee, String eventId) {
        String attendeeId = attendee.getAttendeeId();
        // get references to the attendee's document and the event's document in the database
        DocumentReference attendeeRef = collection.document(attendeeId);
        DocumentReference eventRef = MainActivity.db.getEventsRef().document(eventId);

        eventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // remove attendee from event and event from attendee
                    eventRef.update("attendees", FieldValue.arrayRemove(attendeeId));
                    attendeeRef.update("events", FieldValue.arrayRemove(eventId));
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }
}
