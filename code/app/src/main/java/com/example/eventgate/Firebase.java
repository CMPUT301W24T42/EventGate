package com.example.eventgate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * This class represents Firebase
 * It allows the app to access features of Firebase
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
     * this holds and instance of MyFirebaseMessagingService
     */
    private MyFirebaseMessagingService messagingService;
    /**
     * this is the reference to the events collection in the database
     */
    private final CollectionReference eventsRef;

    /**
     * this is the reference to the attendees collection in the database
     */
    private final CollectionReference attendeesRef;
    /**
     * this is the reference to the fcmTokens collection in the database
     */
    private final CollectionReference fcmTokensRef;
    /**
     * this is the reference to the alerts collection in the database
     */
    private final CollectionReference alertsRef;
    /**
     * this is the reference to the admins collection in the database
     */
    private final CollectionReference adminsRef;
    /**
     * this is the reference to the images collection in the database
     */
    private final CollectionReference imagesRef;
    /**
     * this holds the info from firebase of the current user
     */
    private FirebaseUser currentUser;

    /**
     * this constructs a new Firebase object
     */
    public Firebase() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.fcm = FirebaseMessaging.getInstance();
        this.messagingService = null;
        this.eventsRef = db.collection("events");
        this.attendeesRef = db.collection("attendees");
        this.fcmTokensRef = db.collection("fcmTokens");
        this.alertsRef = db.collection("alerts");
        this.adminsRef = db.collection("admins");
        this.imagesRef = db.collection("images");
        this.currentUser = null;
    }

    /**
     * this gets an instance of the firestore database
     * @return an instance of the database
     */
    public FirebaseFirestore getDB() {
        return this.db;
    }

    /**
     * this gets an instance of firebase authentication
     * @return an instance of FirebaseAuth
     */
    public FirebaseAuth getmAuth() {
        return this.mAuth;
    }

    /**
     * this gets an instance of firebase cloud messaging
     * @return an instance of FirebaseCloudMessaging
     */
    public FirebaseMessaging getFcm() {
        return this.fcm;
    }

    /**
     * this sets the Firebase Messaging Service
     * @param service the firebase messaging service
     */
    public void setMessagingService(MyFirebaseMessagingService service) {
        this.messagingService = service;
    }

    /**
     * this gets the firebase messaging service
     * @return an instance of MyFirebaseMessagingService
     */
    public MyFirebaseMessagingService getMessagingService() {
        return this.messagingService;
    }

    /**
     * this gets a reference to the events collection in the database
     * @return a collection reference to the events collection
     */
    public CollectionReference getEventsRef() {
        return this.eventsRef;
    }

    /**
     * this gets a reference to the attendees collection in the database
     * @return a collection reference to the attendees collection
     */
    public CollectionReference getAttendeesRef() {
        return this.attendeesRef;
    }

    /**
     * this gets a reference to the fcmTokens collection in the database
     * @return a collection reference to the fcmTokens collection
     */
    public CollectionReference getFcmTokensRef() {
        return this.fcmTokensRef;
    }

    /**
     * this gets a reference to the alerts collection in the database
     * @return a collection reference to the alerts collection
     */
    public CollectionReference getAlertsRef() {
        return this.alertsRef;
    }

    /**
     * this gets a reference to the admins collection in the database
     * @return a collection reference to the admins collection
     */
    public CollectionReference getAdminsRef() {
        return adminsRef;
    }

    /**
     * this gets a reference to the images collection in the database
     * @return a collection reference to the images collection
     */
    public CollectionReference getImagesRef() {
        return imagesRef;
    }

    /**
     * this sets the current user of the app
     * @param user the user to be set
     */
    public void setUser(FirebaseUser user) {
        currentUser = user;
    }

    /**
     * this gets the current user of the app
     * @return an instance of FirebaseUser
     */
    public FirebaseUser getUser() {
        return currentUser;
    }
}
