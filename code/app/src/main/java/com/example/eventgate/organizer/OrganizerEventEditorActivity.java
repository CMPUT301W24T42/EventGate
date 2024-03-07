// This class manages the edit event organizer activity

package com.example.eventgate.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventgate.Firebase;
import com.example.eventgate.R;
import com.example.eventgate.attendee.AttendeeEventListAdapter;
import com.example.eventgate.event.Event;
import com.example.eventgate.event.EventDB;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

/**
 * Activity for the organizer's event page.
 * Allows the organizer to view and edit a given event.
 */
public class OrganizerEventEditorActivity extends AppCompatActivity {

    private TextView eventTitle;
    private Button backButton;

    private String eventId;
    private DocumentReference eventRef;
    ArrayList<String> attendeeDataList;
    ListView attendeeList;
    ArrayAdapter<String> attendeeAdapter;

    /**
     * Called when the activity is starting.
     * Initializes the activity layout and views.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_edit_event);

        eventTitle = findViewById(R.id.EventListViewTitle);

        Intent intent = getIntent();
        eventTitle.setText(intent.getStringExtra("eventName"));
        eventId = intent.getStringExtra("eventId");

        attendeeDataList = new ArrayList<>();
        attendeeList = findViewById(R.id.attendeeListView);
        attendeeAdapter = new AttendeeListAdapter(this, attendeeDataList);
        attendeeList.setAdapter(attendeeAdapter);

        eventRef = new EventDB().getCollection().document(eventId);
        eventRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null) {
                    attendeeDataList.clear();
                    ArrayList<String> attendeeIds = (ArrayList<String>) value.get("attendees");
                    if (attendeeIds != null) {
                        for (String attendeeId : attendeeIds) {
                            FirebaseFirestore.getInstance().collection("attendees")
                                    .document(attendeeId).get().addOnSuccessListener(documentSnapshot -> {
                                attendeeDataList.add(documentSnapshot.getString("name"));
                                // If it's the last element, notify the array adapter
                                if (attendeeId.equals(attendeeIds.get(attendeeIds.size() - 1))) {
                                    attendeeAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }
            }
        });

        backButton = findViewById(R.id.OrganizerEditBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
