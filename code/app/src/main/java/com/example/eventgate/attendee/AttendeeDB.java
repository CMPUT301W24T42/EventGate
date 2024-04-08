package com.example.eventgate.attendee;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eventgate.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


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
