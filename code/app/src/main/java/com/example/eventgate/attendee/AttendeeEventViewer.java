package com.example.eventgate.attendee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.eventgate.R;
import com.example.eventgate.event.EventDB;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
//citations
//https://www.geeksforgeeks.org/how-to-use-picasso-image-loader-library-in-android/
public class AttendeeEventViewer extends AppCompatActivity {
    private TextView textViewEventName;
    private String eventID;
    private String eventName;
    Button back_button, viewAttendeesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee_event_viewer);



        //extract event info
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eventID = extras.getString("EventID");
            eventName = extras.getString("EventName");

        }

        //eventID = "t4sVMhhFRrZIO7VjulOd";

        displayEventPosters(eventID);

        TextView eventNameTitle = findViewById(R.id.eventNameTitle);
        eventNameTitle.setText(eventName);

        back_button = findViewById(R.id.attendee_back_button);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        //no announcement or detail functionality yet, add placeholders
        TextView eventDetailsTextview = findViewById(R.id.eventDetailsTextview);;
        eventDetailsTextview.setText("Event Starts at 9:00am");


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




    }
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

    private void setupViewPager(List<String> posterImageUrls) {
        ViewPager viewPager = findViewById(R.id.viewPager);
        PosterPagerAdapter adapter = new PosterPagerAdapter(this, posterImageUrls);
        viewPager.setAdapter(adapter);
    }

}
