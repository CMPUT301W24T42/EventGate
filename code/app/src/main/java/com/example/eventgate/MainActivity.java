package com.example.eventgate;

import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
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
import com.example.eventgate.attendee.AttendeeEventListAdapter;
import com.example.eventgate.organizer.OrganizerMainMenuActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.checkerframework.checker.units.qual.A;

import java.util.HashMap;
import java.util.List;

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
    public Attendee attendee;
    /**
     * this is the launcher that requests permission to receive notifications
     */
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    // display a message confirming that user has chosen not to receive notifications
                    Toast.makeText(this, "EventGate will not send notifications", Toast.LENGTH_SHORT).show();
                }
            });

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

        // create notification channels
        createEventNotifChannel();
        createMilestoneNotifChannel();
        // ask the user to permission to receive notifications from the app
        askNotificationPermission();

        // store the installation id in shared preferences
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(s -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (!preferences.contains("FirebaseInstallationId")) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("FirebaseInstallationId", s);
                editor.apply();
            }
        });

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

    /**
     * called when the activity is visible to the user, immediately after onCreate
     */
    @Override
    protected void onStart() {
        super.onStart();

        // get deviceId
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String deviceId = preferences.getString("FirebaseInstallationId", "null");

        // get reference to attendees collection
        CollectionReference attendeesRef = db.getAttendeesRef();

        if (!deviceId.equals("null")) {
            // if user is an existing user, get their info from the attendees collection in the database,
            //      otherwise, create a new attendee profile
            attendeesRef
                    .whereEqualTo("deviceId", deviceId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // no existing attendee profile with this deviceId, then create a new attendee profile
                            if (task.getResult().isEmpty()) {
                               createNewAttendee(attendeesRef, deviceId);
                            }
                            else {  // existing user, then get attendee info
                                List<DocumentSnapshot> documents =  task.getResult().getDocuments();
                                for (DocumentSnapshot doc : documents) {
                                    attendee = new Attendee(doc.getString("name"), deviceId, doc.getId());
                                }
                            }
                        }
                    });
        }


        db.setMessagingService(new MyFirebaseMessagingService());

        mAuth = db.getmAuth();

        // Check if user is signed in (non-null) and update UI accordingly.
        if (mAuth.getCurrentUser() == null) {
            signInUser();
        }
        else {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            db.setUser(currentUser);
            updateUI(currentUser, adminButton);
        }

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
                    new NotificationPermissionFragment().show(getSupportFragmentManager(), "REQUEST NOTIFICATION PERMISSION");
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
    private void createNewAttendee(CollectionReference attendeesRef, String deviceId) {
        String attendeeId = attendeesRef.document().getId();
        HashMap<String, Object> data = new HashMap<>();
        data.put("deviceId", deviceId);
        // set an attendee's name to their id by default until the user enters it later in user settings
        data.put("name", attendeeId);
        attendeesRef.document(attendeeId).set(data)
                .addOnSuccessListener(unused ->
                        Log.d("Firebase Firestore", "Attendee has been added successfully!"))
                .addOnFailureListener(e -> Log.d(TAG, "Event could not be added!" + e));
        attendee = new Attendee(attendeeId, deviceId, attendeeId);
    }
}