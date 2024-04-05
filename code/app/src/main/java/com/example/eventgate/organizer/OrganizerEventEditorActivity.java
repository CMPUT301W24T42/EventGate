package com.example.eventgate.organizer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventgate.MainActivity;
import com.example.eventgate.R;
import com.example.eventgate.event.EventDB;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Activity for the organizer's event page.
 * Allows the organizer to view and edit a given event.
 */
public class OrganizerEventEditorActivity extends AppCompatActivity implements CreateAlertFragment.OnAlertCreatedListener {

    private TextView eventTitle;
    private Button backButton, uploadPosterButton, editQRButton;
    private Button createAlert;
    private String eventName;
    private String eventId;
    private String eventDescription;
    private ArrayList<OrganizerAlert> alerts;
    private DocumentReference eventRef;
    ArrayList<String> attendeeDataList;
    ListView attendeeList;
    ArrayAdapter<String> attendeeAdapter;
    private static final int PICK_IMAGE_REQUEST = 1;

    /**
     * Called when the activity is starting.
     * Initializes the activity layout and views.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_edit_event);

        eventTitle = findViewById(R.id.EventListViewTitle);

        Intent intent = getIntent();
        eventName = intent.getStringExtra("eventName");
        eventTitle.setText(eventName);
        eventId = intent.getStringExtra("eventId");
        eventDescription = intent.getStringExtra("eventDescription");
        TextView eventDetailsText =  findViewById(R.id.EventDetails);
        TextView attendanceCount = findViewById(R.id.attendance_text_view);
        eventDetailsText.setText(eventDescription);
        alerts = (ArrayList<OrganizerAlert>) intent.getSerializableExtra("alerts");

        attendeeDataList = new ArrayList<>();
        attendeeList = findViewById(R.id.attendeeListView);
        attendeeAdapter = new AttendeeListAdapter(this, attendeeDataList);
        attendeeList.setAdapter(attendeeAdapter);

        if (eventId != null) {
            eventRef = new EventDB().getCollection().document(eventId);
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
                        if (attendeeIds != null) {
                            for (String attendeeId : attendeeIds) {
                                FirebaseFirestore.getInstance().collection("attendees")
                                        .document(attendeeId).get().addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                // Retrieve name and check in count
                                                String attendeeName = documentSnapshot.getString("name");
                                                Object eventCheckInNumberMapObject = documentSnapshot.get("eventCheckInNumber");

                                                if (eventCheckInNumberMapObject instanceof Map) {
                                                    Map<?, ?> eventCheckInNumberMap = (Map<?, ?>) eventCheckInNumberMapObject;
                                                    // Check if eventId exists in the map
                                                    Object checkInCountObject = eventCheckInNumberMap.get(eventId);

                                                    if (checkInCountObject instanceof Number) {
                                                        int checkInCount = ((Number) checkInCountObject).intValue();
                                                        String checkInCountString = Integer.toString(checkInCount);
                                                        attendeeDataList.add(attendeeName + " - Check-ins: " + checkInCountString);
                                                    } else {
                                                        attendeeDataList.add(attendeeName + " - Check-ins: 0");
                                                    }

                                                } else {
                                                    Log.d("Firestore", "eventCheckInNumber is not a Map");
                                                }

                                                // If it's the last element, notify the array adapter
                                                if (attendeeId.equals(attendeeIds.get(attendeeIds.size() - 1))) {
                                                    attendeeAdapter.notifyDataSetChanged();
                                                }

                                            } else {
                                                Log.d("Firestore", "Document does not exist");
                                            }
                                        });
                            }
                            // update number of attendees attending the event
                            int attendeeCount = attendeeIds.size();
                            attendanceCount.setText(Integer.toString(attendeeCount));
                        }
                    }
                }
            });
        }

        uploadPosterButton = findViewById(R.id.button_upload_poster);
        uploadPosterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateImageUploadProcess();
            }
        });

        editQRButton = findViewById(R.id.OrganizerEditQRButton);
        editQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerEventEditorActivity.this, OrganizerEditQR.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("eventName", eventName);
                startActivity(intent);
            }
        });

        backButton = findViewById(R.id.OrganizerEditBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // show the dialog for creating a new alert
        createAlert = findViewById(R.id.button_create_alert);
        createAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateAlertFragment fragment = new CreateAlertFragment();
                // create a bundle so we can access the eventId in the dialog fragment
                Bundle args = new Bundle();
                args.putString("eventId", eventId);
                fragment.setArguments(args);
                fragment.show(getSupportFragmentManager(), "CREATE ALERT");
            }
        });

    }

    /**
     * begins process of uploading posters
     */
    //these 5 functions handle storing uploaded photos
    private void initiateImageUploadProcess() {
        openFileChooser();
    }

    /**
     * starts intent for file browser for image
     */
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * handles file browser for image intent
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            //Upload Image to Firebase
            uploadImageToFirebase(imageUri);
        }
    }

    /**
     * calculates unique hash/storage path in firebase db for poster and saves
     * @param imageUri uploaded image
     */
    private void uploadImageToFirebase(Uri imageUri) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference fileReference = storageRef.child("images/" + System.currentTimeMillis() + ".jpg");

        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();

                    saveImageReferenceInFirestore(downloadUrl);
                    Toast.makeText(OrganizerEventEditorActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> Toast.makeText(OrganizerEventEditorActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Saves a reference to poster location in firebase db in event collection in firestore
     * @param downloadUrl
     */
    private void saveImageReferenceInFirestore(String downloadUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> poster = new HashMap<>();
        poster.put("url", downloadUrl);

        db.collection("events").document(eventId).collection("posters").add(poster)
                .addOnSuccessListener(documentReference -> Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w("TAG", "Error adding document", e));

        // save image to images collection
        CollectionReference imagesRef = MainActivity.db.getImagesRef();
        String imagesId =  imagesRef.document().getId();
        HashMap<String, Object> data = new HashMap<>();
        data.put("url", downloadUrl);
        data.put("eventId", eventId);
        imagesRef.document(imagesId).set(data)
                .addOnSuccessListener(unused -> Log.d("Firestore", "Image has been added successfully!"))
                .addOnFailureListener(e -> Log.d("Firestore", "Image could not be added!" + e));
    }

    /**
     * Callback method to handle the addition of a new alert.
     * @param alert The alert to be added.
     */
    @Override
    public void onAlertCreated(OrganizerAlert alert) {
        // add alert to data list
        alerts.add(alert);

        // get references to firebase collections
        CollectionReference alertsRef = MainActivity.db.getAlertsRef();

        // get alert data that will be stored in firebase
        HashMap<String, String> newAlert = new HashMap<>();
        newAlert.put("title", alert.getTitle());
        newAlert.put("body", alert.getMessage());
        newAlert.put("channelId", alert.getChannelId());
        newAlert.put("organizerId", alert.getOrganizerId());

        // sent to events collection
        sendToEventsCollection(newAlert);

        // send to alerts collection
        String alertId = alertsRef.document().getId();
        newAlert.put("eventId", eventId);
        alertsRef
                .document(alertId)
                .set(newAlert)
                .addOnSuccessListener(unused -> Log.d("EventDB", "Alert has been sent to alerts collection"));


    }

    /**
     * stores the alert in the events collection of the database
     * @param newAlert the alert to be stored
     */
    private void sendToEventsCollection(HashMap<String, String> newAlert) {
        // get reference to the events collection
        CollectionReference eventsRef = MainActivity.db.getEventsRef();
        // send to events collection
        eventsRef
                .document(eventId)
                .update("alerts", FieldValue.arrayUnion(newAlert))
                .addOnSuccessListener(unused -> Log.d("EventDB", "Alert has been sent to events collection"));
    }

}
