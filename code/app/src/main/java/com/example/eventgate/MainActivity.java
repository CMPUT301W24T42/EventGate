package com.example.eventgate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.eventgate.admin.AdminActivity;
import com.example.eventgate.attendee.AttendeeActivity;
import com.example.eventgate.organizer.OrganizerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    Button attendeeButton;
    Button organizerButton;
    Button adminButton;
    private FirebaseAuth mAuth;
    public static final String TAG = "Firebase Auth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        attendeeButton = findViewById(R.id.attendee_button);
        organizerButton = findViewById(R.id.organizer_button);
        adminButton = findViewById(R.id.admin_button);

        // Start each activity with an intent.
        attendeeButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,
                AttendeeActivity.class)));

        organizerButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,
                OrganizerActivity.class)));

        adminButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,
                AdminActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            signInUser();
        }
        else {
//            updateUI(currentUser);
        }

    }

    /**
     * Signs in the user using Firebase anonymous authentication
     */
    private void signInUser() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update the UI
                            Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}