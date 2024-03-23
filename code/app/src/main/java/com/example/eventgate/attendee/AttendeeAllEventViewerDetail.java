package com.example.eventgate.attendee;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.eventgate.MainActivity;
import com.example.eventgate.R;
import com.example.eventgate.event.Event;
import com.example.eventgate.event.EventDB;
import com.example.eventgate.organizer.OrganizerAlert;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * this is for displaying individual events in detail for an attendee chosen from all events list
 * gives attendee option to sign up(but not in) for event
 */
public class AttendeeAllEventViewerDetail extends AppCompatActivity {
    private TextView textViewEventName;
    private String eventID;
    private String eventName;
    private ArrayList<OrganizerAlert> alertsDataList;
    private ListView alertsList;
    private ArrayAdapter<OrganizerAlert> alertsAdapter;
    Button back_button, viewAttendeesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee_event_viewer_detailed);



        //extract event info
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eventID = extras.getString("EventID");
            eventName = extras.getString("EventName");
            alertsDataList = (ArrayList<OrganizerAlert>) extras.getSerializable("alerts");
        }


        alertsList = findViewById(R.id.alertList);
        alertsAdapter = new AlertListAdapter(this, alertsDataList);
        alertsList.setAdapter(alertsAdapter);

        isAttendeeRegistered();



        displayEventPosters(eventID);

        TextView eventNameTitle = findViewById(R.id.eventNameTitle);
        eventNameTitle.setText(eventName);

        TextView eventDetailsTextview = findViewById(R.id.eventDetailsTextview);


        back_button = findViewById(R.id.attendee_back_button);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        TextView eventDetailsTv = findViewById(R.id.eventDetailsTextview);
        EventDB eventDB = new EventDB();

        CompletableFuture<String> eventDetailsFuture = eventDB.getEventDetailsDB(eventID);


        eventDetailsFuture.thenAccept(eventDetails -> {

            runOnUiThread(() -> {
                if (eventDetails != null) {
                    eventDetailsTv.setText(eventDetails);
                } else {
                    eventDetailsTv.setText("Details not found for event ID: " + eventID);
                }
            });
        }).exceptionally(e -> {

            e.printStackTrace();
            runOnUiThread(() -> eventDetailsTv.setText("Failed to load event details."));
            return null;
        });

        //view attendees button
        viewAttendeesButton = findViewById(R.id.attendeeViewParticipantsButton);
        viewAttendeesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeAllEventViewerDetail.this, AttendeeViewParticipants.class);
                intent.putExtra("eventID", eventID);
                intent.putExtra("eventName", eventName);
                startActivity(intent);
            }
        });

        CollectionReference collection = MainActivity.db.getAlertsRef();

        // snapshot listener that adds/updates all the alerts from the database
        collection
                .whereEqualTo("eventId", eventID)
                .addSnapshotListener((value, error) -> {
                    for (QueryDocumentSnapshot doc : value) {
                        if (doc.get("title") != null && Objects.equals(doc.get("channelId"), "event_channel")) {
                            String title = doc.getString("title");
                            String body = doc.getString("body");
                            String channelId = doc.getString("channelId");
                            String organizerId = doc.getString("organizerId");
                            OrganizerAlert alert = new OrganizerAlert(title, body, channelId, organizerId, eventID);
                            alertsDataList.add(alert);
                            alertsAdapter.notifyDataSetChanged();
                        }
                    }
                });

    }

    private void isAttendeeRegistered() {
        FirebaseUser currentUser = MainActivity.db.getmAuth().getCurrentUser();
        if (currentUser == null) {

            return;
        }
        String userId = currentUser.getUid();
        EventDB eventDB = new EventDB();

        eventDB.isAttendeeSignedUp(userId, eventID).thenAccept(isRegistered -> {
            runOnUiThread(() -> {
                Button signupButton = findViewById(R.id.signupButton);
                if (isRegistered) {
                    signupButton.setText("Already signed up");
                    signupButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                    signupButton.setEnabled(false);
                }
            });
        }).exceptionally(exception -> {
            Log.e("YourActivity", "Error checking event registration status", exception);
            return null;
        });
    }

    /**
     * queries current event in firestore for all its poster paths, then use paths to find
     * stored posters in firebase db, then display for attendee
     * @param eventId event's unique id
     */
    private void displayEventPosters(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> posterImageUrls = new ArrayList<>();

        db.collection("events").document(eventId).collection("posters")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String imageUrl = document.getString("url");
                                posterImageUrls.add(imageUrl);
                            }

                            setupViewPager(posterImageUrls);
                        } else {
                            Log.w("test", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    /**
     * connects viewpager in ui with posterpageradapter and array of posterURLs
     * @param posterImageUrls array of poster locations in firebase db
     */
    private void setupViewPager(List<String> posterImageUrls) {
        ViewPager viewPager = findViewById(R.id.viewPager);
        PosterPagerAdapter adapter = new PosterPagerAdapter(this, posterImageUrls);
        viewPager.setAdapter(adapter);
    }
}
