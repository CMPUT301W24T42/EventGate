/**
 * Activity for the organizer's main menu.
 */

package com.example.eventgate.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventgate.event.Event;
import com.example.eventgate.event.EventDB;
import com.example.eventgate.R;
import com.google.firebase.installations.FirebaseInstallations;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;


/**
 * Allows the organizer to view and manage events, including creating new events and editing existing ones.
 * It utilizes fragments for creating new events and communicates with Firebase for event data management.
 * Outstanding issues: There are no outstanding issues currently known.
 */
public class OrganizerMainMenuActivity extends AppCompatActivity implements OrganizerCreateEventFragment.OnEventAddedListener {
    Button createNewEventButton;
    Button organizerMainMenuBackButton;
    ArrayList<Event> eventDataList;
    ListView eventList;
    ArrayAdapter<Event> eventAdapter;

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
        eventList = findViewById(R.id.EventListView);

        eventDataList = new ArrayList<>();
        eventAdapter = new EventListAdapter(this, eventDataList);
        eventList.setAdapter(eventAdapter);

        updateOrganizerEvents();

        createNewEventButton.setOnClickListener(v -> {
            OrganizerCreateEventFragment dialogFragment = new OrganizerCreateEventFragment();
            dialogFragment.show(getSupportFragmentManager(), "popup_dialog");
        });

        organizerMainMenuBackButton.setOnClickListener(v ->
                finish()
        );

        eventList.setOnItemClickListener((parent, view, position, id) -> {
            Event clickedEvent = eventDataList.get(position);
            Intent intent = new Intent(OrganizerMainMenuActivity.this, OrganizerEventEditorActivity.class);
            intent.putExtra("eventId", clickedEvent.getEventId());
            intent.putExtra("eventName", clickedEvent.getEventName());
            intent.putExtra("eventDescription", clickedEvent.getEventDescription());
            intent.putExtra("alerts", clickedEvent.getAlerts());
            startActivity(intent);
        });
    }

    /**
     * Callback method to handle the addition of a new event.
     *
     * @param event The event to be added.
     */
    @Override
    public void onEventAdded(Event event) {
        eventDataList.add(event);
        eventAdapter.notifyDataSetChanged();

        // Save event and check-in QR code data to Firebase using EventDB
        EventDB eventDB = new EventDB();
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(id -> eventDB.AddOrganizerEvent(event, id));
    }

    /**
     * Updates the list of organizer events by fetching data from Firebase.
     */
    public void updateOrganizerEvents() {
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(id -> {
            CompletableFuture<ArrayList<Event>> attendeeEvents = new EventDB().getOrganizerEvents(id);
            attendeeEvents.thenAccept(r -> {
                eventDataList.clear();
                eventDataList.addAll(r);
                eventAdapter.notifyDataSetChanged();
            });
        });
    }
}
