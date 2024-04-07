package com.example.eventgate.organizer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventgate.R;
import com.example.eventgate.event.EventDB;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class OrganizerMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        Bundle extras = getIntent().getExtras();
        eventId = extras.getString("eventId");

        MapView mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        mapView.getMapAsync(this);

        Button backButton = findViewById(R.id.mapBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        CompletableFuture<ArrayList<Map<String, Object>>> locations =  new EventDB().getLocations(eventId);
        locations.thenAccept(r -> {
            LatLng attendee = null;
            for (Map<String, Object> location : r) {
                GeoPoint attendeeLocation = (GeoPoint) location.get("location");
                attendee = new LatLng(attendeeLocation.getLatitude(), attendeeLocation.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(attendee).title((String) location.get("name")));
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(attendee));
        });
    }
}
