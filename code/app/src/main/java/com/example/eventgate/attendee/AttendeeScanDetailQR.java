package com.example.eventgate.attendee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.eventgate.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AttendeeScanDetailQR extends AppCompatActivity {
    private String eventId;
    private String eventName;
    private String eventDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_attendee_scan_detail_qr);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView eventNameTitle = findViewById(R.id.scanDetailQREventNameTitle);
        TextView eventDetailsTextView = findViewById(R.id.scanDetailsQREventDetailsTextview);

        Button backButton = findViewById(R.id.attendee_qr_detail_back_button);

        Intent previousIntent = getIntent();
        if (previousIntent != null) {
            eventId = previousIntent.getStringExtra("eventId");
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        assert eventId != null;
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the event name and details as a string from Firestore
                        eventName = documentSnapshot.getString("name");
                        eventDetails = documentSnapshot.getString("eventDetails");
                    }
                });

//        eventNameTitle.setText(eventName);
//        eventDetailsTextView.setText(eventDetails);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AttendeeScanDetailQR.this, AttendeeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}