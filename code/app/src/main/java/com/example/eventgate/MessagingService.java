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
    private FirebaseMessaging fcm;
    private CollectionReference fcmTokensRef;
    private FirebaseAuth mAuth;
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

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        sendTokenToFirebase(token);
    }

    /**
     * this subscribes a user to a topic that allows them to receive notifications from the
     * associated event
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

}
