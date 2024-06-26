package com.example.eventgate.attendee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.eventgate.MainActivity;
import com.example.eventgate.R;
import com.example.eventgate.event.EventDB;
import com.example.eventgate.organizer.OrganizerAlert;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

//citations
//https://www.geeksforgeeks.org/how-to-use-picasso-image-loader-library-in-android/
public class AttendeeEventViewer extends AppCompatActivity {
    private TextView textViewEventName;
    private String eventID;
    private String eventName;
    private ArrayList<OrganizerAlert> alertsDataList;
    private ListView alertsList;
    private ArrayAdapter<OrganizerAlert> alertsAdapter;
    Button back_button, viewAttendeesButton;
    ViewPager viewPager;
    ImageView defaultImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee_event_viewer);



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

        // show dialog displaying alert's message once the title of the alert is clicked on the listview
        alertsList.setOnItemClickListener((parent, view, position, id) -> {
            ViewAnnouncementDialog dialog = new ViewAnnouncementDialog();
            Bundle args = new Bundle();
            args.putString("alertTitle", alertsDataList.get(position).getTitle());
            args.putString("alertMessage", alertsDataList.get(position).getMessage());
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "View Announcement Dialog");
        });

        defaultImageView = findViewById(R.id.default_imageview);

        viewPager = findViewById(R.id.viewPager);

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
                    eventDetailsTv.setText("");
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
                Intent intent = new Intent(AttendeeEventViewer.this, AttendeeViewParticipants.class);
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
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            defaultImageView.setVisibility(View.GONE);
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String imageUrl = document.getString("url");
                                posterImageUrls.add(imageUrl);
                            }

                            setupViewPager(posterImageUrls);
                        } else {
                            viewPager.setVisibility(View.GONE);
                            defaultImageView.setImageResource(R.drawable.default_viewpager);
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
        PosterPagerAdapter adapter = new PosterPagerAdapter(this, posterImageUrls);
        viewPager.setAdapter(adapter);
    }



}
