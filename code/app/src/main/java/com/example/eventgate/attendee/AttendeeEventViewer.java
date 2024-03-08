package com.example.eventgate.attendee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.eventgate.R;
import com.example.eventgate.event.EventDB;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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

        //test getattendees
        System.out.println(eventID);
        CompletableFuture<List<String>> attendeesFuture = new EventDB().getAttendeesForEvent(eventID);

        // When the CompletableFuture completes, print the attendees
        attendeesFuture.thenAccept(attendees -> {
            attendees.forEach(System.out::println); // This will print each attendee to the console
        }).exceptionally(e -> {
            System.out.println("Failed to fetch attendees: " + e.getMessage());
            e.printStackTrace();
            return null;
        });









    }
}
