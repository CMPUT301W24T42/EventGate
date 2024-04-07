package com.example.eventgate.attendee;

import com.example.eventgate.MainActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;


/**
 * This is used to add, remove, and retrieve attendee data from the database
 */
public class AttendeeDB {
    /**
     * Constructs a new AttendeeDB
     */
    public AttendeeDB() {
    }

    public void getAttendeeInfo(String deviceId, Attendee attendee) {
        CollectionReference attendeesRef = MainActivity.db.getAttendeesRef();
        attendeesRef.whereEqualTo("deviceId", deviceId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    attendee.setName(document.getString("name"));
                    attendee.setHomepage(document.getString("homepage"));
                    attendee.setEmail(document.getString("email"));
                    attendee.setPhoneNumber(document.getString("phoneNumber"));
                    attendee.setProfilePicture(document.getString("profilePicturePath"));
                }
            }
        });
    }
}
