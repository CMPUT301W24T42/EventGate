package com.example.eventgate;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
    }

    /**
     * this is used to send registration tokens to the Firestore database for storage
     *
     * @param token the token to be stored in the database
     */
    private void sendTokenToFirebase(String token) {
        HashMap<String, String> data = new HashMap<>();
        data.put("registrationToken", token);
        if (MainActivity.db.getUser() != null) {
            String userId = MainActivity.db.getUser().getUid();
            fcmTokensRef
                    .document(userId)
                    .set(data)
                    .addOnSuccessListener(unused -> Log.d(TAG, "Token has been sent successfully!"))
                    .addOnFailureListener(e -> Log.d(TAG, "Token could not be sent!" + e));
        }
    }

    /**
     * This updates a user's registration token on initial app startup and when an existing token
     * is changed
     *
     * @param token The token used for sending messages to this application instance. This token is
     *              the same as the one retrieved by {@link FirebaseMessaging#getToken()}.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);  // Log
        sendTokenToFirebase(token);  // send new token to firebase for storage
    }

    /**
     * this subscribes a user to a topic that allows them to receive notifications from the
     * associated event
     *
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
     * creates notifications when messaging service receives a message
     *
     * @param message Remote message that has been received.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        // log
        Log.d(TAG, "Notification received");
        // get title and body of the notification from the remote message
        RemoteMessage.Notification notification = message.getNotification();
        String title = notification.getTitle();
        String body = notification.getBody();
        String channelId = notification.getChannelId();
        if (!Objects.equals(channelId, MILESTONE_CHANNEL_ID) || !Objects.equals(channelId, EVENT_CHANNEL_ID)) {
            channelId = EVENT_CHANNEL_ID;
        }
        String organizerId = message.getData().get("organizerId");
        // create and show the notification to the user
        createNotification(title, body, channelId, organizerId);
    }

    /**
     * builds and shows a notification to the user on three conditions
     * 1. user must have post notifications enabled
     * 2. if it is an event alert, the user must not also be the organizer of the event
     * 3. if it is a milestone alert, the user must be the organizer
     *
     * @param title       the title of the notification
     * @param body        the message of the notification
     * @param channelId   the notification channel that the notification will be sent through
     * @param organizerId the id of the organizer who the alert is associated with
     */
    private void createNotification(String title, String body, String channelId, String organizerId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String deviceId = preferences.getString("FirebaseInstallationId", "null");

        if (!shouldBeBuilt(title, channelId, deviceId, organizerId)) {
            return;
        }
        // if the user has enabled post notifications then the notification will be built
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            // build the notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            // create a random number to serve as the notification id, we will not store or keep
            //      track of this number as we won't need to update or delete notifications
            Random notificationId = new Random();

            // make the notification appear
            NotificationManagerCompat.from(this).notify(notificationId.nextInt(), builder.build());
        }
    }

    /**
     * used to determine if a notification should be built and shown to a user based on three conditions:
     *      1. if it is an alert for the deletion of an event, send the alert to all users associated with the event
     *      2. if it is an event alert, the user must not also be the organizer of the event
     *      3. if it is a milestone alert, the user must be the organizer
     * @param title the title of the notification
     * @param channelId the notification channel that the notification will be sent through
     * @param organizerId the id of the organizer that the notification is associated with
     * @param deviceId the firebase installation id of the organizer associated with the notification
     * @return a completable future instance that will contain a boolean that is true if the notification
     *
     */
    private boolean shouldBeBuilt(String title, String channelId, String deviceId, String organizerId) {
        // if it's an alert that an event has been cancelled, send it to all users related to the event
        if (title.equals("Event Cancelled!") || title.equals("Attendance Revoked!")) {
            return true;
        }
        // only builds notification for milestones if the current device belongs to the organizer of the event
        else if (channelId.equals(MILESTONE_CHANNEL_ID) && !deviceId.equals(organizerId)) {
            return false;
        }
        // prevents organizers from getting alerts for their own events
        else if (channelId.equals(EVENT_CHANNEL_ID) && deviceId.equals(organizerId)) {
            return false;
        }
        return true;
    }

}