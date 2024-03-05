package com.example.eventgate.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.eventgate.Event.Event;
import com.example.eventgate.R;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {
    ArrayList<Event> eventDataList;
    ListView eventList;
    ArrayAdapter<Event> eventAdapter;
    Button adminActivityBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        eventList = findViewById(R.id.event_list);
        adminActivityBackButton = findViewById(R.id.admin_back_button);

        eventDataList = new ArrayList<>();

        eventAdapter = new AdminEventListAdapter(this, eventDataList);
        eventList.setAdapter(eventAdapter);

        adminActivityBackButton.setOnClickListener(v -> finish());
    }
}