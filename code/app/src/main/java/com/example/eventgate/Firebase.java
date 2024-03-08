package com.example.eventgate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * This class represents Firebase
 * It allows the app to access the Firestore database and use Firebase authentication
 */

public class Firebase {
    /**
     * this holds the firestore database
     */
    private final FirebaseFirestore db;
    /**
     * this holds an instance of FirebaseAuth
     */
    private final FirebaseAuth mAuth;
    /**
     * this holds an instance of the Firebase Cloud Messaging
     */
    private final FirebaseMessaging fcm;
    /**
     * this is the reference to the events collection in the database
     */
    private final CollectionReference eventsRef;
    private final CollectionReference fcmTokensRef;

    /**
     * this constructs a new Firebase object
     */
    public Firebase() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.fcm = FirebaseMessaging.getInstance();
        this.eventsRef = db.collection("events");
        this.fcmTokensRef = db.collection("fcmTokens");
    }

    public FirebaseFirestore getDB() {
        return this.db;
    }

    public FirebaseAuth getmAuth() {
        return this.mAuth;
    }

    public FirebaseMessaging getFcm() {
        return this.fcm;
    }

    public CollectionReference getEventsRef() {
        return this.eventsRef;
    }

    public CollectionReference getFcmTokensRef() {
        return this.fcmTokensRef;
    }
}
