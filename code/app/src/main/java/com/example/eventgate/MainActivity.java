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
import android.widget.Button;
import android.widget.Toast;

import com.example.eventgate.admin.AdminActivity;
import com.example.eventgate.attendee.AttendeeActivity;
import com.example.eventgate.organizer.OrganizerMainMenuActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

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

        db.setMessagingService(new MyFirebaseMessagingService());

        mAuth = db.getmAuth();

        // Check if user is signed in (non-null) and update UI accordingly.
        if (mAuth.getCurrentUser() == null) {
            signInUser();
        }
        else {
//            updateUI(currentUser);
            db.setUser(mAuth.getCurrentUser());
            // TODO: check for admin permission and update ui accordingly
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
//                            updateUI(user);
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
}