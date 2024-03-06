package com.example.eventgate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Firebase {
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private final CollectionReference eventsRef;

    public Firebase() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.eventsRef = db.collection("events");
    }

    public FirebaseFirestore getDB() {
        return this.db;
    }

    public FirebaseAuth getmAuth() {
        return this.mAuth;
    }

    public CollectionReference getEventsRef() {
        return this.eventsRef;
    }
}
