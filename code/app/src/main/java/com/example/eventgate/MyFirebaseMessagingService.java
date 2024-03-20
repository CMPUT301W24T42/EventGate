package com.example.eventgate;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.eventgate.event.Event;
import com.example.eventgate.organizer.CreateAlertFragment;
import com.example.eventgate.organizer.OrganizerAlert;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * this class takes care of functions regarding firebase cloud messaging and post notifications
 */
public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService implements CreateAlertFragment.OnAlertCreatedListener{
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
        eventListener();
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
//        createEventNotifChannel();
//        createMilestoneNotifChannel();
        // get title and body of the notification from the remote message
        RemoteMessage.Notification notification = message.getNotification();
        String title = notification.getTitle();
        String body = notification.getBody();
        String channelId = notification.getChannelId();
        String organizerId = message.getData().get("organizerId");
        // create and show the notification to the user
        createNotification(title, body, channelId, organizerId);
    }

    private void eventListener() {
        CollectionReference eventRef = MainActivity.db.getEventsRef();

        eventRef.addSnapshotListener((EventListener<QuerySnapshot>) (value, error) -> {
            if (error != null) {
                Log.e("Messaging Service", error.toString());
                return;
            }
            if (value != null) {
                // List of milestones
                ArrayList<Integer> milestones = new ArrayList<>(Arrays.asList(1, 5, 10, 25, 50, 100));

                for (QueryDocumentSnapshot document : value) {
                    // Get the event name and attendee IDs for each document
                    String eventName = document.getString("name");
                    String eventId = document.getString("eventId");
                    Object attendeesObject = document.get("attendees");
                    Object milestonesObject = document.get("milestones");
                    if (milestonesObject instanceof ArrayList) {
                        // arraylist stores values as Long in firestore so switch it back to Integers
                        ArrayList<Integer> currentMilestones = getIntegers((ArrayList<Long>) milestonesObject);
                        if (attendeesObject instanceof ArrayList) {
                            ArrayList<String> attendeeIds = (ArrayList<String>) attendeesObject;

                            // create a milestone alert if the number of attendees is a milestone
                            //      that has not already been acknowledged by the database
                            Integer attendeeCount = attendeeIds.size();
                            if (milestones.contains(attendeeCount) && !currentMilestones.contains(attendeeCount)) {
                                createMilestoneAlert(attendeeCount, eventName, eventId);
                                eventRef
                                        .document(eventId)
                                        .update("milestones", FieldValue.arrayUnion(attendeeCount));
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * used to turn a list of Longs into a list of integers
     * @param milestonesObject the list of Longs that will be changed to integers
     * @return a list of integers
     */
    @NonNull
    private static ArrayList<Integer> getIntegers(ArrayList<Long> milestonesObject) {
        // arraylist stores values as Long in firestore so switch it back to Integers
        ArrayList<Long> currentMilestonesLong = milestonesObject;
        ArrayList<Integer> currentMilestones = new ArrayList<>();
        for (Long milestoneLong : currentMilestonesLong) {
            Integer milestoneInteger = milestoneLong.intValue();
            currentMilestones.add(milestoneInteger);
        }
        return currentMilestones;
    }

    /**
     * builds and shows a notification to the user on three conditions
     *      1. user must have post notifications enabled
     *      2. if it is an event alert, the user must not also be the organizer of the event
     *      3. if it is a milestone alert, the user must be the organizer
     * @param title the title of the notification
     * @param body the message of the notification
     * @param channelId the notification channel that the notification will be sent through
     * @param organizerId the id of the organizer who the alert is associated with
     */
    private void createNotification(String title, String body, String channelId, String organizerId) {
        // check for scenarios where the notification should not be built
        CompletableFuture<Boolean> shouldCreate = notifShouldBeBuilt(channelId, organizerId);
        shouldCreate.thenAccept(result -> {
            // notification should not be built
            if (!result) {
                return;
            }
            // if the user has disabled post notifications then the notification will not be built
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
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
                });

    }

    /**
     * used to determine if a notification should be built and shown to a user based on two conditions:
     *      1. if it is an event alert, the user must not also be the organizer of the event
     *      3. if it is a milestone alert, the user must be the organizer
     * @param channelId the notification channel that the notification will be sent through
     * @param organizerId the id of the organizer that the notification is associated with
     * @return a completable future instance that will contain a boolean that is true if the notification
     *          should be built and shown, or false if it should not
     */
    private CompletableFuture<Boolean> notifShouldBeBuilt(String channelId, String organizerId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        AtomicBoolean create = new AtomicBoolean(true);

        // get the deviceId and check for scenarios where a notification should not be built
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(s -> {
            // only builds notification for milestones if the current device belongs to the organizer of the event
            if (channelId.equals(MILESTONE_CHANNEL_ID) && !s.equals(organizerId)) {
                create.set(false);
            }
            // prevents organizers from getting alerts for their own events
            if (channelId.equals(EVENT_CHANNEL_ID) && s.equals(organizerId)) {
                create.set(false);
            }
            future.complete(create.get());
        });

        return future;
    }

    /**
     * creates an alert that notifies organizers of milestones that their events reach
     * @param attendeeCount the number of attendees currently checked into the organizer's event
     * @param eventName the name of the event
     * @param eventId the id of the event
     */
    private void createMilestoneAlert(int attendeeCount, String eventName, String eventId) {
        String title = "Milestone reached!";
        // determine the syntax of the message
        String attendeeString = (attendeeCount == 1) ? "attendee" : "attendees";
        String message = String.format(Locale.US,"%s has reached %d %s.", eventName, attendeeCount, attendeeString);
        // create the alert
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(id -> {
            OrganizerAlert alert = new OrganizerAlert(title, message, "milestone_channel", id, eventId);
            ((CreateAlertFragment.OnAlertCreatedListener) this).onAlertCreated(alert);
        });
    }

    /**
     * Callback method to handle the addition of a new alert.
     * @param alert The alert created.
     */
    @Override
    public void onAlertCreated(OrganizerAlert alert) {
        // get reference to firebase alerts collection
        CollectionReference alertsRef = MainActivity.db.getAlertsRef();

        // get alert data that will be stored in firebase
        HashMap<String, String> newAlert = new HashMap<>();
        newAlert.put("title", alert.getTitle());
        newAlert.put("body", alert.getMessage());
        newAlert.put("channelId", alert.getChannelId());
        newAlert.put("organizerId", alert.getOrganizerId());
        newAlert.put("eventId", alert.getEventId());

        // send to alerts collection
        String alertId = alertsRef.document().getId();
        alertsRef
                .document(alertId)
                .set(newAlert)
                .addOnSuccessListener(unused -> Log.d("EventDB", "Alert has been sent to alerts collection"));

    }
}
