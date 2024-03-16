package com.example.eventgate;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

/**
 * this class takes care of functions regarding firebase cloud messaging and post notifications
 */
public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
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
    /**
     * the name of the notification channel for events
     */
    private final String EVENT_CHANNEL_ID = "event_channel";
    /**
     * the name of the notification channel for organizer milestones
     */
    private final String MILESTONE_CHANNEL_ID = "milestone_channel";

    /**
     * this creates a new MyFirebaseMessagingService object
     */
    public MyFirebaseMessagingService() {
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
                    String msg = "Your token is " + token;
                    Log.d(TAG, msg);
                });

    }

    /**
     * this is used to send registration tokens to the Firestore database for storage
     * @param token the token to be stored in the database
     */
    public void sendTokenToFirebase(String token) {
        HashMap<String, String> data = new HashMap<>();
        data.put("registrationToken", token);
        String userId = MainActivity.db.getUser().getUid();
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
        Log.d(TAG, "Refreshed token: " + token);  // Log
        sendTokenToFirebase(token);  // send new token to firebase for storage
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
     * @param eventId the id of the event that the user will be unsubscribed from
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

    /**
     * creates notifications when messaging service receives a message
     * @param message Remote message that has been received.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        // log
        Log.d(TAG, "Notification received");
        // create notification channels
        createEventNotifChannel();
        createMilestoneNotifChannel();
        // get title and body of the notification from the remote message
        String title = Objects.requireNonNull(message.getNotification()).getTitle();
        String body = message.getNotification().getBody();
        String channelId = message.getNotification().getChannelId();
        // create and show the notification to the user
        createNotification(title, body, channelId);
    }

    private void createEventNotifChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Event Alerts";
            String description = "Alerts regarding events";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(EVENT_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createMilestoneNotifChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Milestone Alerts";
            String description = "Alerts that are sent once your events reach a milestone";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(MILESTONE_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void createNotification(String title, String body, String channelId) {
        // build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // if the user has disabled post notifications then the built notification will not show
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // create a random number to serve as the notification id
        Random notificationId = new Random();
        // make the notification appear
        NotificationManagerCompat.from(this).notify(notificationId.nextInt(), builder.build());
    }

}
