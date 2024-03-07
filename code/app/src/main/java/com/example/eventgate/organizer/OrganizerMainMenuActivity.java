package com.example.eventgate.organizer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventgate.Event.Event;
import com.example.eventgate.Event.EventDB;
import com.example.eventgate.R;
import com.google.firebase.firestore.FirebaseFirestore;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for the organizer's main menu.
 * Allows the organizer to view and manage events.
 */
public class OrganizerMainMenuActivity extends AppCompatActivity implements OrganizerCreateEventFragment.OnEventAddedListener {
    Button createNewEventButton;
    Button organizerMainMenuBackButton;
    ListView eventListView;
    EventListAdapter eventListAdapter;
    ArrayList<String> events;

    /**
     * Called when the activity is starting.
     * Initializes the activity layout and views.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_main_menu);

        // Initialize Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        createNewEventButton = findViewById(R.id.CreateEventButton);
        organizerMainMenuBackButton = findViewById(R.id.OrganizerMainMenuBackButton);
        eventListView = findViewById(R.id.EventListView);

        events = new ArrayList<>();
        eventListAdapter = new EventListAdapter(this, events);
        eventListView.setAdapter(eventListAdapter);

        createNewEventButton.setOnClickListener(v -> {
            OrganizerCreateEventFragment dialogFragment = new OrganizerCreateEventFragment();
            dialogFragment.setOnEventAddedListener(this);
            dialogFragment.show(getSupportFragmentManager(), "popup_dialog");
        });

        organizerMainMenuBackButton.setOnClickListener(v ->
                finish()
        );
    }

    /**
     * Callback method to handle the addition of a new event.
     *
     * @param event The event to be added.
     */
    @Override
    public void onEventAdded(Event event, Bitmap eventQRBitmap) {
        events.add(event.getEventName());
        eventListAdapter.notifyDataSetChanged();

        if (eventQRBitmap != null) {
            // Save event and check-in QR code data to Firebase using EventDB
            EventDB eventDB = new EventDB();
            eventDB.AddOrganizerEvent(event, eventQRBitmap);
        } else {
            // Handle the case where eventQRBitmap is null
            Toast.makeText(this, "Event QR Bitmap is null", Toast.LENGTH_SHORT).show();
        }
    }
}
