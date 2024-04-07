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
import android.widget.Toast;

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
import com.google.firebase.installations.FirebaseInstallations;

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
    private Integer attendanceLimit;
    private Integer registrationCount;
    private ArrayList<OrganizerAlert> alertsDataList;
    private ListView alertsList;
    private ArrayAdapter<OrganizerAlert> alertsAdapter;
    Button back_button, viewAttendeesButton;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee_event_viewer_detailed);

        FirebaseInstallations.getInstance().getId().addOnSuccessListener(id -> {
             userId = id;
        });

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

        //this checks whether to grey out signup button
        isAttendeeRegistered(userId);

        // Retrieve the attendance limit and registration count
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        assert eventID != null;
        db.collection("events").document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Object attendanceLimitObject = documentSnapshot.get("attendanceLimit");
                        Object registrationCountObject = documentSnapshot.get("registrationCount");

                        if (attendanceLimitObject instanceof Long) {
                            // Attendance limit is stored as a Long (which is the Firestore representation for Integer)
                            Long attendanceLimitLong = (Long) attendanceLimitObject;
                            attendanceLimit = attendanceLimitLong.intValue();

                        } else if (attendanceLimitObject instanceof Integer) {
                            // Attendance limit is stored as an Integer
                            attendanceLimit = (Integer) attendanceLimitObject;
                        }

                        // Do the same for registration count
                        if (registrationCountObject instanceof Long) {
                            // Registration Count is stored as a Long
                            Long registrationCountLong = (Long) registrationCountObject;
                            registrationCount = registrationCountLong.intValue();

                        } else if (registrationCountObject instanceof Integer) {
                            // Attendance limit is stored as an Integer
                            registrationCount = (Integer) registrationCountObject;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                });

        Button signupButton = findViewById(R.id.signupButton);

        //signing up will need 2 functions since registered events are stored under attendees and events
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Attendance limit of -1 indicates unlimited attendance
                if (attendanceLimit == -1 || registrationCount < attendanceLimit) {
                    EventDB eventDB = new EventDB();
                    CompletableFuture<Void> registerAttendeeFuture = eventDB.registerAttendee(AttendeeAllEventViewerDetail.this, userId, eventID);
                    CompletableFuture<Void> registerAttendee2Future = eventDB.registerAttendee2(userId, eventID);

                    CompletableFuture.allOf(registerAttendeeFuture, registerAttendee2Future)
                            .thenRun(() -> {
                                // Call isAttendeeRegistered here to ensure it runs after registration is confirmed.
                                isAttendeeRegistered(userId);
                            })
                            .exceptionally(e -> {
                                // Handle any exceptions here
                                Log.e("AttendeeAllEventViewerDetail", "Error registering attendee", e);
                                return null;
                            });
                } else {
                    Toast.makeText(AttendeeAllEventViewerDetail.this, "This Event is full!", Toast.LENGTH_SHORT).show();
                }
            }
        });

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

    /**
     * checks if attendee is signed up for the event
     * @param userId fid
     */
    private void isAttendeeRegistered(String userId) {


        //getting fid must be asyncrhoinous, otherwise will be null
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(id -> {
            String userId2 = id;
            EventDB eventDB = new EventDB();

            eventDB.isAttendeeSignedUp(userId2, eventID).thenAccept(isRegistered -> {
                runOnUiThread(() -> {
                    Button signupButton = findViewById(R.id.signupButton);
                    if (isRegistered) {
                        signupButton.setText("Already signed up");
                        signupButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                        signupButton.setEnabled(false);
                    } else {

                    }
                });
            }).exceptionally(exception -> {
                Log.e("YourActivity", "Error checking event registration status", exception);
                return null;
            });
        }).addOnFailureListener(e -> {

            Log.e("YourActivity", "Failed to get Firebase Installation ID", e);
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
