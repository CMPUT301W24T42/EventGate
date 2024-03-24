package com.example.eventgate.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.eventgate.MainActivity;
import com.example.eventgate.R;
import com.example.eventgate.attendee.Attendee;
import com.example.eventgate.attendee.PosterPagerAdapter;
import com.example.eventgate.event.EventDB;
import com.example.eventgate.organizer.OrganizerAlert;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.common.StringUtils;

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
    /**
     * this is a reference to the posters subcollection of this specific event
     */
    CollectionReference postersRef;
    /**
     * tag for logging
     */
    final String TAG = "AdminEventViewerActivity";

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

        postersRef = MainActivity.db.getEventsRef().document(eventId).collection("posters");

        // sends admin back to the main menu
        backButton = findViewById(R.id.event_back_button);
        backButton.setOnClickListener(v -> finish());

        // set name and details of event
        TextView eventTitle = findViewById(R.id.event_title);
        TextView detailsTextview = findViewById(R.id.event_details_textview);
        eventTitle.setText(eventName);
        detailsTextview.setText(eventDetails);

        // set up viewpager
        setupViewPager();

        // display posters
        displayEventPosters();

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
     * gets image urls from firestore and displays images in viewpager
     */
    private void displayEventPosters() {
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
                        Log.d(TAG, "get failed with ", task.getException());
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
        String imageUrl = posterImageUrls.get(currentPoster);
        posterPagerAdapter.removePoster(currentPoster);
        viewPager.setAdapter(posterPagerAdapter);

        // remove poster from firebase database
        deletePosterFromFirestore(imageUrl);

        // remove poster from firebase storage
        deletePosterFromCloudStorage(imageUrl);
    }

    /**
     * this deletes the event poster from firestore
     * @param imageUrl the url of the image to be deleted
     */
    private void deletePosterFromFirestore(String imageUrl) {
        postersRef.whereEqualTo("url", imageUrl).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                snapshot.getReference().delete()
                        .addOnSuccessListener(unused -> {
                            Log.d(TAG, "Event poster successfully deleted from Firestore");
                        })
                        .addOnFailureListener(e -> {
                            Log.d(TAG, "Error deleting event poster from Firestore", e);
                        });
            }
        });
    }

    /**
     * this deletes the event poster from firebase cloud storage
     * @param imageUrl the url of the image to be deleted
     */
    private void deletePosterFromCloudStorage(String imageUrl) {
        // get the name of the image from the url of the image
        String imageName = imageUrl.substring(imageUrl.indexOf("images%2F") + 9, imageUrl.indexOf(".jpg?"));

        // get reference to the image in firebase cloud storage and delete it
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference fileReference = storageRef.child("images/" + imageName + ".jpg");
        Log.d(TAG, String.valueOf(fileReference));
        fileReference.delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Event poster successfully deleted from Storage");
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Error deleting event poster from Storage", e);
                });

    }
}