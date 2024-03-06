package com.example.eventgate.organizer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventgate.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
     * @param eventName The name of the event to be added.
     */
    @Override
    public void onEventAdded(String eventName, Bitmap eventQRBitmap) {
        events.add(eventName);
        eventListAdapter.notifyDataSetChanged();

        // Save event and check-in QR code data to Firebase
        saveEventToFirestore(eventName, eventQRBitmap);

    }

    private void saveEventToFirestore(String eventName, Bitmap eventQRBitmap) {
        // Convert bitmap to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        eventQRBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] byteArray = baos.toByteArray();

        // Get a reference to the Firestore database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new document with the event name as the document ID
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventName", eventName);
        eventData.put("checkInQRCode", byteArray);
        eventData.put("organizer", ""); // Set organizer field to blank
        eventData.put("attendees", ""); // Set attendees field to blank

        db.collection("events")
                .document(eventName) // Use event name as document ID
                .set(eventData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Event and check-in QR code data saved successfully
                        Toast.makeText(OrganizerMainMenuActivity.this, "Event added successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to save event and check-in QR code data
                        Toast.makeText(OrganizerMainMenuActivity.this, "Failed to add event", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
