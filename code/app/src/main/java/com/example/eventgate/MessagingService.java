package com.example.eventgate;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.HashMap;
import java.util.Objects;

public class MessagingService extends FirebaseMessagingService {
    /**
     * this holds an instance of FirebaseMessaging
     */
    private FirebaseMessaging fcm;
    /**
     * this is a reference to the fcmTokens collection in the database
     */
    private CollectionReference fcmTokensRef;
    /**
     * this holds an instance of FirebaseAuth
     */
    private FirebaseAuth mAuth;
    /**
     * a tag for logging
     */
    final String TAG = "Firebase Cloud Messaging";

    public MessagingService() {
        this.fcm = MainActivity.db.getFcm();
        this.fcmTokensRef = MainActivity.db.getFcmTokensRef();
        this.mAuth = MainActivity.db.getmAuth();
        fcm.getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get FCM registration token
                    String token = task.getResult();
                    sendTokenToFirebase(token);

                    // Log
                    String msg = "Your token is" + token;
                    Log.d(TAG, msg);
                });
    }

    /**
     * this is used to send registration tokens to the Firestore database for storage
     * @param token the token to be stored in the database
     */
    public void sendTokenToFirebase(String token) {
        HashMap<String, String> data = new HashMap<>();
        data.put("deviceToken", token);
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        fcmTokensRef
                .document(userId)
                .set(data)
                .addOnSuccessListener(unused -> Log.d(TAG, "Token has been sent successfully!"))
                .addOnFailureListener(e -> Log.d(TAG, "Token could not be sent!" + e));
    }

    /**
     * This updates a user's registration token on initial app startup and when an existing token
     *     is changed
     * @param token The token used for sending messages to this application instance. This token is
     *     the same as the one retrieved by {@link FirebaseMessaging#getToken()}.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        sendTokenToFirebase(token);
    }

    /**
     * this subscribes a user to a topic that allows them to receive notifications from the
     *     associated event
     * @param eventId the id of the event that the user will be subscribed to
     */
    public void addUserToTopic(String eventId) {
        fcm.subscribeToTopic(eventId)
                .addOnCompleteListener(task -> {
                    String msg = "Subscribed";
                    if (!task.isSuccessful()) {
                        msg = "Subscribe failed";
                    }
                    Log.d(TAG, msg);
                });
    }

    /**
     * this unsubscribes a user from a topic so they stop receiving notifications from the
     *     associated event
     * @param eventId the id of the event that theu user will be unsubscribed from
     */
    public void removeUserFromTopic(String eventId) {
        fcm.unsubscribeFromTopic(eventId)
                .addOnCompleteListener(task -> {
                    String msg = "Unsubscribed";
                    if (!task.isSuccessful()) {
                        msg = "Unsubscribe failed";
                    }
                    Log.d(TAG, msg);
                });
    }



}
