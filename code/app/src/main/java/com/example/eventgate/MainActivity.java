package com.example.eventgate;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.eventgate.admin.AdminActivity;
import com.example.eventgate.attendee.AttendeeActivity;
import com.example.eventgate.organizer.OrganizerMainMenuActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

        // ask the user to permission to receive notifications from the app
        askNotificationPermission();

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
        // Check if user is signed in (non-null) and update UI accordingly.

        mAuth = db.getmAuth();

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
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }


}