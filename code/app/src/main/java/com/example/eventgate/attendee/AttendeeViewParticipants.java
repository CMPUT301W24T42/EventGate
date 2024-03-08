package com.example.eventgate.attendee;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventgate.R;
import com.example.eventgate.event.EventDB;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
/**
 * Activity for attendee to view all participants of events they themselves are in
 */
public class AttendeeViewParticipants extends AppCompatActivity {

    Button back_button;

    private ListView lvAttendees;
    private AttendeeViewParticipantsListAdapter adapter;
    private ArrayList<String> attendees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee_view_event_participants);

        String eventID = getIntent().getStringExtra("eventID");

        String eventName = getIntent().getStringExtra("eventName");

        TextView eventNameTitle = findViewById(R.id.eventNameTitle);
        eventNameTitle.setText(eventName);


        back_button = findViewById(R.id.attendee_back_button);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        lvAttendees = (ListView) findViewById(R.id.attendeeList);
        attendees = new ArrayList<>();
        adapter = new AttendeeViewParticipantsListAdapter(this, attendees);

        lvAttendees.setAdapter(adapter);

        updateAttendeesList(eventID);
    }

    /**
     * queries firestore for all attendees of an event
     * @param eventId unique eventID
     *
     */
    private void updateAttendeesList(String eventId) {
        CompletableFuture<List<String>> attendeesFuture = new EventDB().getAttendeesForEvent(eventId);

        attendeesFuture.thenAccept(fetchedAttendees -> { // Use a different name here
            runOnUiThread(() -> {
                attendees.clear(); // refers to the member variable
                attendees.addAll(fetchedAttendees); // use the fetchedAttendees
                adapter.notifyDataSetChanged();
            });
        }).exceptionally(e -> {
            Log.e("updateAttendeesList", "Failed to fetch attendees", e);
            runOnUiThread(() -> {
                Toast.makeText(AttendeeViewParticipants.this, "Error fetching attendees.", Toast.LENGTH_LONG).show();
            });
            return null;
        });
    }
}
