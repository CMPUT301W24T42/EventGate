package com.example.eventgate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.eventgate.Admin.AdminActivity;
import com.example.eventgate.Attendee.AttendeeActivity;
import com.example.eventgate.Organizer.OrganizerMainMenuActivity;

public class MainActivity extends AppCompatActivity {
    Button attendeeButton;
    Button organizerButton;
    Button adminButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}