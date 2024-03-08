// This class manages the edit event organizer activity

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

import com.example.eventgate.Firebase;
import com.example.eventgate.R;
import com.example.eventgate.attendee.AttendeeEventListAdapter;
import com.example.eventgate.event.Event;
import com.example.eventgate.event.EventDB;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
/**
 * Activity for the organizer's event page.
 * Allows the organizer to view and edit a given event.
 */
public class OrganizerEventEditorActivity extends AppCompatActivity {

    private TextView eventTitle;
    private Button backButton, uploadPosterButton;

    private String eventId;
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
        eventTitle.setText(intent.getStringExtra("eventName"));
        eventId = intent.getStringExtra("eventId");

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
                                            attendeeDataList.add(documentSnapshot.getString("name"));
                                            // If it's the last element, notify the array adapter
                                            if (attendeeId.equals(attendeeIds.get(attendeeIds.size() - 1))) {
                                                attendeeAdapter.notifyDataSetChanged();
                                            }
                                        });
                            }
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

        
        backButton = findViewById(R.id.OrganizerEditBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
    }

}
