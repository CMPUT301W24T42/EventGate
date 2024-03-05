package com.example.eventgate.organizer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventgate.R;

import java.util.ArrayList;

public class OrganizerMainMenuActivity extends AppCompatActivity implements OrganizerCreateEventFragment.OnEventAddedListener {
    Button createNewEventButton;
    Button organizerMainMenuBackButton;
    ListView eventListView;
    EventListAdapter eventListAdapter;
    ArrayList<String> events;
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
            dialogFragment.show(getSupportFragmentManager(), "popup_dialog");
        });

        organizerMainMenuBackButton.setOnClickListener(v ->
                finish()
        );
    }

    @Override
    public void onEventAdded(String eventName) {
        events.add(eventName);
        eventListAdapter.notifyDataSetChanged();
    }
}
