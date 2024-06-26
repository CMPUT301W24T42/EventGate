package com.example.eventgate.event;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.eventgate.MainActivity;
import com.example.eventgate.MyFirebaseMessagingService;
import com.example.eventgate.attendee.AttendeeDB;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.eventgate.MainActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

    /**
     * Constructs a new EventDB
     */
    public EventDB() {
        db = MainActivity.db.getDB();
        collection = MainActivity.db.getEventsRef();
    }

    /**
     * Adds an organizer event to the database.
     *
     * @param event         The event object containing details of the event.
     * @param deviceId      The organizer's firebase installation id
     */
    public void AddOrganizerEvent(Event event, String deviceId) {
        String eventId = collection.document().getId();
        event.setEventId(eventId);

        // add the organizer to eventid topic so that they can receive alerts for event milestones
        MyFirebaseMessagingService messagingService = MainActivity.db.getMessagingService();
        messagingService.addUserToTopic(eventId);

        HashMap<String, Object> data = new HashMap<>();
        data.put("eventId", event.getEventId());
        data.put("name", event.getEventName());
        data.put("description", event.getEventDescription());
        data.put("organizer", deviceId); // Set organizer field to firebase installation id
        data.put("attendees", new ArrayList<String>()); // Set attendees field to blank
        data.put("registeredUsers", new ArrayList<String>()); // Set registeredAttendees field to blank
        data.put("eventDetails", event.getEventDetails());
        data.put("milestones", new ArrayList<Integer>());
        data.put("attendanceLimit", event.getEventAttendanceLimit());
        data.put("registrationCount", 0);
        data.put("locations", new ArrayList<HashMap<String, Object>>());
        data.put("alerts", new ArrayList<HashMap<String, Object>>());
        data.put("trackingEnabled", event.getGeolocation());
        System.out.println(event.getEventDetails());
        System.out.println(event.getEventId());

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
    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    public CompletableFuture<Integer> checkInAttendee(String deviceId, String eventId, Activity activity) {
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
                            // Increment check-in number
                            incrementCheckInNumber(attendee.getId(), eventId);
                            break;
                        }
                    }
                    if (!alreadyExists) {
                        // Add event to attendee collection
                        Map<String, Object> updates = new HashMap<>();
                        attendeeEvents.add(eventId);
                        updates.put("events", attendeeEvents);
                        db.collection("attendees").document(attendee.getId()).update(updates);

                        // Increment check-in number
                        incrementCheckInNumber(attendee.getId(), eventId);

                        // subscribe attendee to the events topic so they can receive notifications
                        MainActivity.db.getMessagingService().addUserToTopic(eventId);

                        // Add attendee to event collection
                        updates = new HashMap<>();
                        ArrayList<String> eventAttendees = (ArrayList<String>) documentSnapshot.get("attendees");
                        eventAttendees.add(attendee.getId());
                        updates.put("attendees", eventAttendees);
                        db.collection("events").document(eventId).update(updates);
                        futureResult.complete(0);

                        // If user has tracking enabled, save their location to the database
                        if ((boolean) attendee.get("trackingEnabled") && (boolean) documentSnapshot.get("trackingEnabled")) {
                            LocationRequest locationRequest = new LocationRequest.Builder(
                                    Priority.PRIORITY_HIGH_ACCURACY,
                                    0
                            ).build();
                            LocationCallback locationCallback = new LocationCallback() {
                                @Override
                                public void onLocationResult(LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    if (locationResult != null && locationResult.getLastLocation() != null) {
                                        String name = (String) attendee.get("name");
                                        double latitude = locationResult.getLastLocation().getLatitude();
                                        double longitude = locationResult.getLastLocation().getLongitude();
                                        saveLocation(documentSnapshot, eventId, name, latitude, longitude);
                                    }
                                }
                            };
                            try {
                                LocationServices.getFusedLocationProviderClient(activity).
                                        requestLocationUpdates(locationRequest, locationCallback, null);
                            } catch (Exception SecurityException) {
                                Log.d("LOCATION", "SECURITY EXCEPTION");
                            }
                        }
                    }
                });
            } else {
                futureResult.complete(1);
            }
        });
        return futureResult;
    }

    private void saveLocation(DocumentSnapshot docSnapshot, String eventId, String name, double latitude, double longitude) {
        HashMap<String, Object> updates = new HashMap<>();
        ArrayList<Map<String, Object>> locations = (ArrayList<Map<String, Object>>) docSnapshot.get("locations");
        HashMap<String, Object> location_info = new HashMap<>();
        GeoPoint location = new GeoPoint(latitude, longitude);
        location_info.put("name", name);
        location_info.put("location", location);
        locations.add(location_info);
        updates.put("locations", locations);
        db.collection("events").document(eventId).update(updates);
    }

    public CompletableFuture<ArrayList<Map<String, Object>>> getLocations(String eventId) {
        CompletableFuture<ArrayList<Map<String, Object>>> locations = new CompletableFuture<>();
        db.collection("events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
           locations.complete((ArrayList<Map<String, Object>>) documentSnapshot.get("locations"));
        });
        return locations;
    }

    /**
     * Increments the check-in number for the attendee for a specific event
     * @param attendeeId The ID of the attendee
     * @param eventId The ID of the event
     */
    private void incrementCheckInNumber(String attendeeId, String eventId) {
        // Construct the field name based on the event ID
        String fieldName = "eventCheckInNumber." + eventId;
        // Use Firestore's FieldValue.increment to atomically increment the check-in number for the event
        db.collection("attendees").document(attendeeId)
                .update(fieldName, FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Check-in number for event incremented successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error incrementing check-in number for event", e));
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
                Log.d("test1", "matching id not found");
                return;
            }
            DocumentSnapshot attendee = queryDocumentSnapshots.getDocuments().get(0);
            ArrayList<String> attendeeEvents = (ArrayList<String>) attendee.get("events");

            if (attendeeEvents.size() == 0) {  // If it's empty, simply return
                Log.d("test1", "is empty");
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
     * signed-up
     * Get a list of events that a user is registered for, given their firebase installation id
     * @param deviceId attendee's firebase installation id
     * @return CompleteableFuture of Arraylist of Events
     * */
    public CompletableFuture<ArrayList<Event>> getRegisteredEvents(String deviceId) {
        Log.d("ID", deviceId);
        CompletableFuture<ArrayList<Event>> futureEvents = new CompletableFuture<>();
        ArrayList<Event> events = new ArrayList<>();
        db.collection("attendees").whereEqualTo("deviceId", deviceId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {  // If there is no matching deviceId, simply return
                        return;
                    }
                    DocumentSnapshot attendee = queryDocumentSnapshots.getDocuments().get(0);
                    ArrayList<String> attendeeEvents = (ArrayList<String>) attendee.get("registeredEvents");

                    if (attendeeEvents.size() == 0) {  // If it's empty, simply return
                        System.out.println("get reg is 0");
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
     * Checks whether a user is signed up for an event
     * @return CompleteableFuture of Arraylist of Events
     * */
    public CompletableFuture<Boolean> isAttendeeSignedUp(String deviceId, String eventId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        db.collection("attendees").whereEqualTo("deviceId", deviceId).get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (queryDocumentSnapshots.isEmpty()) {  // If there is no matching deviceId, simply return
                                        return;
                                    }
                                    String attendeeId = queryDocumentSnapshots.getDocuments().get(0).getId();

                                    List<String> registeredUsers = (List<String>) documentSnapshot.get("registeredUsers");
                                    if (registeredUsers != null && registeredUsers.contains(attendeeId)) {
                                        System.out.println("true");
                                        future.complete(true);
                                    } else {
                                        System.out.println("false");
                                        future.complete(false);
                                    }
                        });
                    } else {
                        System.out.println("Event document not found.");
                        future.complete(false);
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error accessing document: " + e.getMessage());
                    future.completeExceptionally(e);
                });
        return future;
    }

    /**
     * This function stores the registered attendee under event
     * and increments the registration count
     * @param userId firebase id
     * @param eventId event id
     * @return future
     */
    public CompletableFuture<Void> registerAttendee(Context context, String deviceId, String eventId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DocumentReference eventDocRef = db.collection("events").document(eventId);

        db.collection("attendees").whereEqualTo("deviceId", deviceId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.isEmpty()) {  // If there is no matching deviceId, simply return
                                return;
                            }
                            String attendeeId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            db.runTransaction(transaction -> {
                                // Get the current registration count
                                DocumentSnapshot eventSnapshot = transaction.get(eventDocRef);
                                Long currentCount = eventSnapshot.getLong("registrationCount");

                                // Handle the case where the registrationCount field is null or doesn't exist
                                long newCount = (currentCount != null) ? currentCount + 1 : 1;

                                // Update the registration count
                                transaction.update(eventDocRef, "registrationCount", newCount);

                                // Add the user to the registeredUsers array
                                transaction.update(eventDocRef, "registeredUsers", FieldValue.arrayUnion(attendeeId));
                                // Complete the transaction
                                return null;
                            }).addOnSuccessListener(result -> {
                                Toast.makeText(context, "You're registered", Toast.LENGTH_SHORT).show();
                                future.complete(null);
                            }).addOnFailureListener(e -> {
                                Toast.makeText(context, "Error while registering, try again: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                future.completeExceptionally(e);
                            });
                });

        return future;
    }


    /**
     * this is 2nd version of register attendee that is also run to store registered attendee under attendee
     * @param deviceId
     * @param eventId
     * @return
     */
    public CompletableFuture<Void> registerAttendee2(String deviceId, String eventId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.collection("attendees").whereEqualTo("deviceId", deviceId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {  // If there is no matching deviceId, simply return
                        return;
                    }
                    String attendeeId = queryDocumentSnapshots.getDocuments().get(0).getId();
                    DocumentReference attendeeDocRef = db.collection("attendees").document(attendeeId);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("registeredEvents", FieldValue.arrayUnion(eventId));

                    attendeeDocRef.set(updates, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                System.out.println("Event successfully added to the attendee's registered events.");
                                future.complete(null);
                            })
                            .addOnFailureListener(e -> {
                                System.out.println("Error updating the attendee document: " + e.getMessage());
                                future.completeExceptionally(e);
                            });
                });
        return future;
    }
  
    /**
     * retrieves event details
     * @param eventID the id of the event
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
        // this gets the list of attendees attending the event and then removes the event from each
        //     of their event lists
        collection.document(eventId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            ArrayList<String> attendees = new ArrayList<>();
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        attendees = (ArrayList<String>) document.get("attendees");
                        removeEvent(attendees, event.getEventId());
                        // after removing the event from the attendees' lists, this removes the event
                        //     from the event collection as well
                        collection.document(eventId)
                                .delete()
                                .addOnSuccessListener(unused -> Log.d(TAG, "Event has been deleted successfully"))
                                .addOnFailureListener(e -> Log.d(TAG, "Error deleting event" + e));
                    } else {
                        Log.e(TAG, "Document does not exist");
                    }
                }else{
                    Log.e(TAG, "Task Failed: " + task.getException());
                }
            }
        });
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
                        newEvent.setEventDescription(doc.getString("description"));
                        events.add(newEvent);
                    }
                    futureEvents.complete(events);
                });
        return futureEvents;
    }

    /**
     * Queries for all attendees of an event
     * @param eventId event's unique ID
     * @return CompleteableFuture of Arraylist of Attendees
     */
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

    /**
     * retrieves all events
     * @return  all events
     */
    public CompletableFuture<ArrayList<Event>> getAllEvents() {
        CompletableFuture<ArrayList<Event>> futureEvents = new CompletableFuture<>();
        ArrayList<Event> events = new ArrayList<>();

        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        futureEvents.complete(events);
                        return;
                    }
                    for (QueryDocumentSnapshot queryResult : queryDocumentSnapshots) {
                        String eventName = queryResult.getString("name");
                        Event event = new Event(eventName);
                        event.setEventId(queryResult.getId());
                        events.add(event);
                    }
                    futureEvents.complete(events);
                }).addOnFailureListener(e -> {

                    futureEvents.completeExceptionally(e);
                });
        return futureEvents;
    }


    /**
     * saves user info in db
     * @param deviceId fid
     * @param name user full name
     * @param phoneNumber user's phone number
     * @param email user's email
     * @param homepage user's website
     * @param hasUpdatedInfo whether user has set info
     * @return
     */
    public CompletableFuture<Void> updateUserInfo(String deviceId, String name, String phoneNumber, String email, String homepage, Boolean hasUpdatedInfo, Boolean trackingEnabled) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.collection("attendees").whereEqualTo("deviceId", deviceId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {  // If there is no matching deviceId, simply return
                        return;
                    }
                    String attendeeId = queryDocumentSnapshots.getDocuments().get(0).getId();
                    DocumentReference userAttributeDocRef = db.collection("attendees").document(attendeeId);


                    Map<String, Object> attributes = new HashMap<>();
                    attributes.put("name", name);
                    attributes.put("phoneNumber", phoneNumber);
                    attributes.put("email", email);
                    attributes.put("homepage", homepage);
                    attributes.put("hasUpdatedInfo", hasUpdatedInfo);
                    attributes.put("trackingEnabled", trackingEnabled);


                    userAttributeDocRef.set(attributes, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                future.complete(null);
                            })
                            .addOnFailureListener(e -> {
                                future.completeExceptionally(e);
                            });
                });

        return future;
    }

    public CompletableFuture<String> retrieveUserNameFromID(String userId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        DocumentReference userAttributeDocRef = db.collection("attendees").document(userId);

        userAttributeDocRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        future.complete(name);
                    } else {
                        future.completeExceptionally(new RuntimeException("User document does not exist for userId: " + userId));
                    }
                })
                .addOnFailureListener(e -> {
                    future.completeExceptionally(e);
                });

        return future;
    }

    /**
     * retrieves all info of user
     * @param deviceId fid
     * @return
     */
    public CompletableFuture<Map<String, Object>> getUserInfo(String deviceId) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        db.collection("attendees").whereEqualTo("deviceId", deviceId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {  // If there is no matching deviceId, simply return
                        return;
                    }
                    String attendeeId = queryDocumentSnapshots.getDocuments().get(0).getId();
                    DocumentReference userDocRef = db.collection("attendees").document(attendeeId);

                    userDocRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                future.complete(document.getData());
                            } else {
                                future.completeExceptionally(new Exception("No such document."));
                            }
                        } else {
                            future.completeExceptionally(task.getException());
                        }
                    });
                });

        return future;
    }

    /**
     * retrieves whether user has set info yet
     * @param deviceId fid
     * @return
     */
    public CompletableFuture<Boolean> getUserInfoUpdateStatus(String deviceId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        db.collection("attendees").whereEqualTo("deviceId", deviceId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {  // If there is no matching deviceId, simply return
                        return;
                    }
                    String attendeeId = queryDocumentSnapshots.getDocuments().get(0).getId();
                    DocumentReference userDocRef = db.collection("attendees").document(attendeeId);

                    userDocRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Boolean isActive = document.getBoolean("hasUpdatedInfo");
                                future.complete(isActive);
                            } else {
                                future.completeExceptionally(new Exception("No such document."));
                            }
                        } else {
                            future.completeExceptionally(task.getException());
                        }
                    });
                });

        return future;
    }

    /**
     * removes the event from attendees list of events in the case that the event is deleted
     * @param attendees a list of attendees who are attending the event that is being removed
     * @param eventId the id of the event to be removed
     */
    private void removeEvent(ArrayList<String> attendees, String eventId) {
        for (String attendee : attendees) {
            collection
                    .document(attendee)
                    .update("events", FieldValue.arrayRemove(eventId));
        }
    }

}