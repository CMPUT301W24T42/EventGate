package com.example.eventgate.Organizer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventgate.R;

public class OrganizerMainMenuActivity extends AppCompatActivity {
    Button createNewEventButton;
    Button organizerMainMenuBackButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_main_menu);

        createNewEventButton = findViewById(R.id.CreateEventButton);
        organizerMainMenuBackButton = findViewById(R.id.OrganizerMainMenuBackButton);

        createNewEventButton.setOnClickListener(v ->
                startActivity(new Intent(OrganizerMainMenuActivity.this, OrganizerCreateEventActivity.class))
        );

        organizerMainMenuBackButton.setOnClickListener(v ->
                finish()
        );
    }
}
