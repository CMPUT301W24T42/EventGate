package com.example.eventgate.admin;

import static com.example.eventgate.admin.DeleteImageFromFirebase.deletePosterFromCloudStorage;
import static com.example.eventgate.admin.DeleteImageFromFirebase.deletePosterFromFirestore;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.eventgate.ConfirmDeleteDialog;
import com.example.eventgate.MainActivity;
import com.example.eventgate.R;
import com.example.eventgate.attendee.Attendee;
import com.example.eventgate.attendee.AttendeeDB;
import com.example.eventgate.attendee.AttendeeEventListAdapter;
import com.example.eventgate.attendee.PosterPagerAdapter;
import com.example.eventgate.event.EventDB;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This is the activity for the admin event viewer
 * It allows the administrator to view an event's posters, details, and attendees.
 * It also allows the administrator to delete posters
 */
public class AdminEventViewerActivity extends AppCompatActivity {
    /**
     * this holds the id of the event
     */
    String eventId;
    /**
     * this holds the name of the event
     */
    ArrayList<Attendee> attendeeDataList;
    /**
     * this is an adapter for displaying a list of attendees
     */
    ArrayAdapter<Attendee> attendeeAdapter;
    ListView attendeeList;
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
     * this is the button for deleting posters
     */
    Button deleteButton;
    /**
     * tag for logging
     */
    final String TAG = "AdminEventViewerActivity";
    ImageView defaultImageView;

    /**
     * Called when the activity is starting.
     * Initializes the activity layout and views.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_viewer);

        // get/set event info
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // find textviews
            TextView eventTitle = findViewById(R.id.event_title);
            TextView detailsTextview = findViewById(R.id.event_details_textview);

            // get/set event name and id
            eventId = extras.getString("eventId");
            String eventName = extras.getString("name");
            eventTitle.setText(eventName);

            // get/set event details
            EventDB eventDB = new EventDB();
            CompletableFuture<String> eventDetailsFuture = eventDB.getEventDetailsDB(eventId);
            eventDetailsFuture.thenAccept(eventDetails -> runOnUiThread(() -> {
                if (eventDetails != null) {
                    detailsTextview.setText(eventDetails);
                } else {
                    detailsTextview.setText(String.format("Details not found for event ID: %s", eventId));
                }
            })).exceptionally(e -> {
                e.printStackTrace();
                runOnUiThread(() -> detailsTextview.setText("Failed to load event details."));
                return null;
            });
        }

        // get a reference to the event's poster subcollection
        postersRef = MainActivity.db.getEventsRef().document(eventId).collection("posters");

        // sends admin back to the main menu
        Button backButton = findViewById(R.id.event_back_button);
        backButton.setOnClickListener(v -> finish());

        // deletes an event poster
        deleteButton = findViewById(R.id.delete_poster_button);
        deleteButton.setOnClickListener(v -> {
            // get a title and message for the confirm delete dialog
            Resources resources = getResources();
            String title = String.format("%s %s?", resources.getString(R.string.delete_title), "this poster");
            String message = String.format("%s %s", "This poster", resources.getString(R.string.delete_message));
            // create a dialog to confirm that the admin wants to delete the poster
            ConfirmDeleteDialog confirmDeleteDialog = ConfirmDeleteDialog.newInstance(title, message);
            confirmDeleteDialog.setOnDeleteClickListener(() -> deleteEventPoster());
            confirmDeleteDialog.show(getSupportFragmentManager(), "CONFIRM DELETE POSTER");
        });

        // set up viewpager
        setupViewPager();

        // display posters
        displayEventPosters();

        defaultImageView = findViewById(R.id.poster_image_view);



        // create the attendee list and set adapter
        createAttendeeList();

        // get a reference to the event's document from the events collection
        DocumentReference eventRef = MainActivity.db.getEventsRef().document(eventId);
        CollectionReference attendeesRef = MainActivity.db.getAttendeesRef();

        // add/update attendees list
        updateAttendeesList(eventRef, attendeesRef);

        // starts a new activity to view event info including attendees of event
        attendeeList.setOnItemClickListener((parent, view, position, id) -> {
            // pop dialog to show all user info
            UserInfoDialog fragment = new UserInfoDialog();
            // create a bundle so we can access
            Bundle args = new Bundle();
            args.putSerializable("attendee", attendeeDataList.get(position));
            fragment.setArguments(args);
            fragment.show(getSupportFragmentManager(), "IMAGE POPUP");  // show dialog
        });


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
        AttendeeDB attendeeDB = new AttendeeDB();
        eventRef.addSnapshotListener((value, error) -> {
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
                                            documentSnapshot.getId());
                                    attendeeDB.getAttendeeInfo((String) documentSnapshot.getData().get("deviceId"), attendee);
                                    attendeeDataList.add(attendee);
                                    attendeeAdapter.notifyDataSetChanged();
                                });

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
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        defaultImageView.setVisibility(View.GONE);
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String imageUrl = document.getString("url");
                            posterImageUrls.add(imageUrl);
                            posterPagerAdapter.notifyDataSetChanged();
                        }
                        if (!posterImageUrls.isEmpty()) {
                            enableButton();
                        }
                    } else {
                            viewPager.setVisibility(View.GONE);
                            defaultImageView.setImageResource(R.drawable.default_viewpager);
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                });
    }

    /**
     * callback method called by the system when the activity enters the "resumed" state
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (posterImageUrls.isEmpty()) {
            disableButton();
        }
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

        if (posterImageUrls.isEmpty()) {
            disableButton();
        }

        // remove poster from firebase database
        deletePosterFromFirestore(imageUrl, postersRef);

        // remove poster from firebase storage
        deletePosterFromCloudStorage(imageUrl);
    }

    private void enableButton() {
            deleteButton.setClickable(true);
            deleteButton.setBackgroundColor(getResources().getColor(R.color.red));
            deleteButton.setTextColor(getResources().getColor(R.color.black));
    }

    private void disableButton() {
        deleteButton.setClickable(false);
        deleteButton.setBackgroundColor(getResources().getColor(R.color.light_gray));
        deleteButton.setTextColor(getResources().getColor(R.color.dark_gray));
    }
}