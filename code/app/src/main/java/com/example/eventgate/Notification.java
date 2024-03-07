package com.example.eventgate;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Objects;

public class Notification {
    private FirebaseMessaging fcm;
    private CollectionReference fcmTokensRef;
    private FirebaseAuth mAuth;
    final String TAG = "Firebase Cloud Messaging";

    public Notification() {
        this.fcm = MainActivity.db.getFcm();
        this.fcmTokensRef = MainActivity.db.getFcmTokensRef();
        this.mAuth = MainActivity.db.getmAuth();
        fcm.getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
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
                    }
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


};
