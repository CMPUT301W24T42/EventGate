package com.example.eventgate.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.eventgate.MainActivity;
import com.example.eventgate.R;
import com.example.eventgate.attendee.Attendee;
import com.example.eventgate.attendee.PosterPagerAdapter;
import com.example.eventgate.event.EventDB;
import com.example.eventgate.organizer.OrganizerAlert;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class AdminEventViewerActivity extends AppCompatActivity {
    /**
     * this holds the id of the event
     */
    String eventId;
    /**
     * this holds the name of the event
     */
    String eventName;
    /**
     * this holds the details of the event
     */
    String eventDetails;
    /**
     * this is an array list that holds attendees
     */
    ArrayList<Attendee> attendeeDataList;
    /**
     * this is the listview that displays the attendees in the attendeeDataList
     */
    ListView attendeeList;
    /**
     * this is an adapter for displaying a list of attendees
     */
    ArrayAdapter<Attendee> attendeeAdapter;
    /**
     * this is the button that sends the user back to AdminActivity
     */
    Button backButton;
    /**
     * this is the button that deletes an event poster
     */
    Button deleteButton;
    /**
     * this is the viewpager to display event posters
     */
    ViewPager viewPager;
    /**
     * this is the adapter for displaying event posters
     */
    PosterPagerAdapter posterPagerAdapter;
    /**
     * this is a list of image urls for the posters
     */
    List<String> posterImageUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_viewer);

        // get event info
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eventId = extras.getString("eventId");
            eventName = extras.getString("name");
            eventDetails = extras.getString("eventDetails");
            // event details not working
            eventDetails = "null";
        }

        // sends admin back to the main menu
        backButton = findViewById(R.id.event_back_button);
        backButton.setOnClickListener(v -> finish());

        // set up viewpager
        setupViewPager();

        // display posters
        displayEventPosters(eventId);

        // deletes an event poster
        deleteButton = findViewById(R.id.delete_poster_button);
        deleteButton.setOnClickListener(v -> {
            deleteEventPoster();
        });



        // create the attendee list and set adapter
        createAttendeeList();

        // get a reference to the event's document from the events collection
        DocumentReference eventRef = MainActivity.db.getEventsRef().document(eventId);
        CollectionReference attendeesRef = MainActivity.db.getAttendeesRef();

        // add/update attendees list
        updateAttendeesList(eventRef, attendeesRef);


    }

    /**
     * creates the attendee list and sets the adapter
     */
    private void createAttendeeList() {
        attendeeDataList = new ArrayList<>();

        attendeeList = findViewById(R.id.user_list);

        attendeeAdapter = new AdminAttendeeListAdapter(this, attendeeDataList, eventId);
        attendeeList.setAdapter(attendeeAdapter);

    }

    /**
     * creates a snapshot listener that updates the list of attendees that are attending the specified event
     * @param eventRef a reference to the document of the event that is being displayed
     * @param attendeesRef a reference to the attendees collection of the database
     */
    private void updateAttendeesList(DocumentReference eventRef, CollectionReference attendeesRef) {
        // add/update the list of attendees attending this specific event
        eventRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null) {
                    attendeeDataList.clear();
                    ArrayList<String> attendeeIds = (ArrayList<String>) value.get("attendees");
                    // there are attendees for the event
                    if (attendeeIds != null) {
                        for (String attendeeId : attendeeIds) {
                            attendeesRef
                                    .document(attendeeId).get().addOnSuccessListener(documentSnapshot -> {
                                        Attendee attendee = new Attendee(documentSnapshot.getString("name"),
                                                documentSnapshot.getString("deviceId"),
                                                documentSnapshot.getString("attendeeId"));
                                        attendeeDataList.add(attendee);
                                        attendeeAdapter.notifyDataSetChanged();
                                    });

                        }

                    }
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
        CollectionReference postersRef = MainActivity.db.getEventsRef().document(eventId).collection("posters");

        postersRef
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String imageUrl = document.getString("url");
                            posterImageUrls.add(imageUrl);
                            posterPagerAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.d("AdminEventViewerActivity", "get failed with ", task.getException());
                    }
                });
    }

    /**
     * creates and sets a posterPagerAdapter for the ViewPager
     */
    private void setupViewPager() {
        viewPager = findViewById(R.id.image_view_pager);
        posterPagerAdapter = new PosterPagerAdapter(this, posterImageUrls);
        viewPager.setAdapter(posterPagerAdapter);
    }

    /**
     * this removes the current poster displayed from the viewpager, firebase storage, and firebase firestore
     */
    private void deleteEventPoster() {
        // remove the poster from the viewpager
        int currentPoster = viewPager.getCurrentItem();
        posterPagerAdapter.removePoster(currentPoster);
        viewPager.setAdapter(posterPagerAdapter);

        // remove poster from firebase database

        // remove poster from firebase storage

    }
}