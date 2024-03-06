package com.example.eventgate.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.eventgate.event.Event;
import com.example.eventgate.R;
import com.example.eventgate.event.EventDB;

import java.util.ArrayList;

/**
 * This is the activity for the administrator main menu.
 * It allows the administrator to view and delete any events or users
 */
public class AdminActivity extends AppCompatActivity {
    /**
     * This is an array list that holds events
     */
    ArrayList<Event> eventDataList;
    /**
     * This is the listview that displays the events in the eventDataList
     */
    ListView eventList;
    /**
     * This is an adapter for displaying a list of events
     */
    ArrayAdapter<Event> eventAdapter;
    /**
     * This is the button used to get back to the Main Menu activity
     */
    Button adminActivityBackButton;
    Button adminDelEventButton;

    /**
     * Called when the activity is starting.
     * Initializes the activity layout and views.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        eventDataList = new ArrayList<>();

        eventList = findViewById(R.id.event_list);
        adminActivityBackButton = findViewById(R.id.admin_back_button);

        eventAdapter = new AdminEventListAdapter(this, eventDataList);
        eventList.setAdapter(eventAdapter);

        adminActivityBackButton.setOnClickListener(v -> finish());
    }
}