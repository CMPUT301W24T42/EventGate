package com.example.eventgate.organizer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventgate.event.Event;
import com.example.eventgate.event.EventDB;
import com.example.eventgate.R;

import java.util.ArrayList;

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
    private Bitmap eventQRBitmap;

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
    public Bitmap onEventAdded(Event event) {
        events.add(event.getEventName());
        eventListAdapter.notifyDataSetChanged();

        // Save event and check-in QR code data to Firebase using EventDB
        EventDB eventDB = new EventDB();
        eventQRBitmap = eventDB.AddOrganizerEvent(event);

        return eventQRBitmap;
    }
}
