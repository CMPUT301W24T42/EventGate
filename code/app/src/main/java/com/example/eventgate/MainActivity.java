package com.example.eventgate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.eventgate.admin.AdminActivity;
import com.example.eventgate.attendee.Attendee;
import com.example.eventgate.attendee.AttendeeActivity;
import com.example.eventgate.attendee.AttendeeDB;
import com.example.eventgate.organizer.OrganizerMainMenuActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.installations.FirebaseInstallations;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * This is the activity for the app's main menu.
 * It allows the user to select their role and  navigate to other activities
 */
public class MainActivity extends AppCompatActivity {
    /**
     * the button that sends a user to the AttendeeActivity
     */
    Button attendeeButton;
    /**
     * the button that sends a user to the OrganizerMainMenuActivity
     */
    Button organizerButton;
    /**
     * the button that sends a user to the AdminActivity
     */
    Button adminButton;
    /**
     * a Firebase object where references are held
     */
    public static final Firebase db = new Firebase();
    /**
     * an instance of firebase authentication
     */
    private FirebaseAuth mAuth;
    /**
     * a tag for logging
     */
    public static final String TAG = "Firebase Auth";
    /**
     * this is an instance of Attendee which holds the current user's info
     */
    public static Attendee attendee;
    private FirebaseUser firebaseUser;

    /**
     * Called when the activity is starting.
     * Initializes the activity layout and views.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        attendeeButton = findViewById(R.id.attendee_button);
        organizerButton = findViewById(R.id.organizer_button);
        adminButton = findViewById(R.id.admin_button);

        mAuth = db.getmAuth();

        db.setMessagingService(new MyFirebaseMessagingService());

        // Check if user is signed in (non-null) and update UI accordingly.
        if (mAuth.getCurrentUser() == null) {
            signInUser();
        }
        else {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            db.setUser(currentUser);
            updateUI(currentUser, adminButton);
        }

        // create notification channels
        createEventNotifChannel();
        createMilestoneNotifChannel();
        // ask the user to permission to receive notifications from the app
        askNotificationPermission();

//        // store the installation id in shared preferences
//        FirebaseInstallations.getInstance().getId().addOnSuccessListener(deviceId -> {
//            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//            if (!preferences.contains("FirebaseInstallationId")) {
//                SharedPreferences.Editor editor = preferences.edit();
//                editor.putString("FirebaseInstallationId", deviceId);
//                editor.apply();
//            }
//            // if there's no attendee info, create a new attendee
//            CollectionReference attendeesRef = db.getAttendeesRef();
//            attendeesRef.whereEqualTo("deviceId", deviceId).get().addOnCompleteListener(task -> {
//                AttendeeDB attendeeDB = new AttendeeDB();
//                Log.d("deviceId", deviceId);
//                if (task.isSuccessful() && task.getResult().isEmpty()) {
//                    createNewAttendee(db.getAttendeesRef(), deviceId, preferences);
//                } else {  // get existing profile for attendee
//                    String name;
//                    String id;
//                    name = preferences.getString("attendeeName", "");
//                    id = preferences.getString("attendeeId", "");
//                    attendee = new Attendee(name, deviceId, id);
//                    attendeeDB.getAttendeeInfo(deviceId, attendee);
//                }
//            });
//        });

        attendeeButton = findViewById(R.id.attendee_button);
        organizerButton = findViewById(R.id.organizer_button);
        adminButton = findViewById(R.id.admin_button);

        // Start each activity with an intent.
        attendeeButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,
                AttendeeActivity.class)));

        organizerButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,
                OrganizerMainMenuActivity.class)));

        adminButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,
                AdminActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // store the installation id in shared preferences
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(deviceId -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (!preferences.contains("FirebaseInstallationId")) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("FirebaseInstallationId", deviceId);
                editor.apply();
            }
            // if there's no attendee info, create a new attendee
            CollectionReference attendeesRef = db.getAttendeesRef();
            attendeesRef.whereEqualTo("deviceId", deviceId).get().addOnCompleteListener(task -> {
                AttendeeDB attendeeDB = new AttendeeDB();
                Log.d("deviceId", deviceId);
                if (task.isSuccessful() && task.getResult().isEmpty()) {
                    createNewAttendee(db.getAttendeesRef(), deviceId, preferences);
                } else {  // get existing profile for attendee
                    String name;
                    String id;
                    name = preferences.getString("attendeeName", "");
                    id = preferences.getString("attendeeId", "");
                    attendee = new Attendee(name, deviceId, id);
                    attendeeDB.getAttendeeInfo(deviceId, attendee);
                }
            });
        });
    }

    /**
     * Signs in the user using Firebase anonymous authentication
     */
    private void signInUser() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update the UI
                        Log.d(TAG, "signInAnonymously:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        firebaseUser = user;
                        updateUI(user, adminButton);
                        db.setUser(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Asks user for permission to receive notifications from the app
     */
    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_DENIED) {
                // create a boolean to keep track of whether a user wants to
                //      be asked for notification permissions, if true then do not ask for permission again
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                boolean dontAskAgain = preferences.getBoolean("dont_ask_again", false);
                if (!dontAskAgain) {
                    // create new fragment that educates the user about the benefits of having notifications
                    //      turned on
                    new NotificationPermissionDialog().show(getSupportFragmentManager(), "REQUEST NOTIFICATION PERMISSION");
                }
            }
        }
    }

    /**
     * creates a notification channel to send event alerts through
     */
    private void createEventNotifChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Event Alerts";
            String description = "Alerts regarding events";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("event_channel", name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * creates a notification channel to send milestone alerts through
     */
    private void createMilestoneNotifChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Milestone Alerts";
            String description = "Alerts that are sent once your events reach a milestone";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("milestone_channel", name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * makes the administrator button visible if the user's uid is in the admins collection in the database
     * @param user the current user of the app
     * @param adminButton the button that will be made visible
     */
    private void updateUI(FirebaseUser user, Button adminButton) {
        String uUid = user.getUid();
        Log.d("UUID", uUid);
        DocumentReference doc = db.getAdminsRef().document(uUid);
        doc.get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            // if a document exists with the user's firebase authentication uuid as the name
            if (document.exists()) {
                adminButton.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * this creates a new attendee profile and stores the info in the attendees collection in the database
     * @param attendeesRef a reference to the attendees collection
     * @param deviceId the firebase installation id of the current user
     */
    private void createNewAttendee(CollectionReference attendeesRef, String deviceId, SharedPreferences preferences) {
        String attendeeId = attendeesRef.document().getId();
        HashMap<String, Object> data = new HashMap<>();
        data.put("deviceId", deviceId);
        // set an attendee's name to their id by default until the user enters it later in user settings
        data.put("name", attendeeId);
        data.put("uUid", firebaseUser.getUid());
        data.put("events", new ArrayList<Integer>());
        data.put("hasUpdatedInfo", false);
        data.put("homepage", "");
        data.put("email", "");
        data.put("phoneNumber", "");
        data.put("registeredEvents", new ArrayList<>());
        attendeesRef.document(attendeeId).set(data)
                .addOnSuccessListener(unused -> {
                    // store info in shared preferences
                    preferences.edit()
                            .putString("attendeeName", attendeeId)
                            .putString("attendeeId", attendeeId)
                            .apply();
                    Log.d("Firebase Firestore", "Attendee has been added successfully!");
                })
                .addOnFailureListener(e -> Log.d("Firebase Firestore", "Attendee could not be added!" + e));
    }

}