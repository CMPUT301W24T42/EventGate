/**
 * This activity allows organizers to reuse existing QR codes for a new event.
 */

package com.example.eventgate.organizer;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventgate.R;
import com.example.eventgate.event.Event;
import com.example.eventgate.event.EventDB;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * Organizers can select from a list of previous events and reuse their check-in QR code
 * for the new event.
 */
public class OrganizerReuseQRActivity extends AppCompatActivity {
    private String newEventId;
    private ArrayAdapter<Event> eventAdapter;
    private ArrayList<Event> eventDataList;
    private String qrCodeDataString;

    /**
     * Called when the activity is starting. Responsible for initializing the activity, views,
     * and retrieving existing event data from Firebase.
     *
     * @param savedInstanceState a Bundle containing the activity's previously saved state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_reuse_qractivity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button backButton = findViewById(R.id.OrganizerReuseQRBackButton);
        Button reuseQRContinueButton = findViewById(R.id.ReuseQRContinue);

        ListView eventListQRs = findViewById(R.id.ReuseQREvents);

        newEventId = getIntent().getStringExtra("eventId");

        eventDataList = new ArrayList<>();
        eventAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventDataList);
        eventListQRs.setAdapter(eventAdapter);

        updateOrganizerEvents();

        eventListQRs.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = eventDataList.get(position);
            String selectedEventId = selectedEvent.getEventId();

            // Get checkInQRCode from firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("events").document(selectedEventId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    qrCodeDataString = documentSnapshot.getString("checkInQRCode");
                }
            });
        });

        reuseQRContinueButton.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            // Update Firestore document for the new event with qrCodeDataString
            if (qrCodeDataString != null) {
                db.collection("events").document(newEventId).update("checkInQRCode", qrCodeDataString)
                    .addOnSuccessListener(aVoid -> {
                        // Successfully updated the document
                    })
                    .addOnFailureListener(e -> {
                        // Failed to update the document
                    });
            }
            finish();
        });

        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Updates the list of organizer events by fetching data from Firebase.
     * Filters out the new event from the list to prevent reusing its own QR code.
     */
    private void updateOrganizerEvents() {
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(id -> {
            CompletableFuture<ArrayList<Event>> organizerEvents = new EventDB().getOrganizerEvents(id);
            organizerEvents.thenAccept(events -> {
                eventDataList.clear();

                // Filter out the new event based on its eventId
                for (Event event : events) {
                    if (!event.getEventId().equals(newEventId)) {
                        eventDataList.add(event);
                    }
                }

                eventAdapter.notifyDataSetChanged();
            });
        });
    }
}