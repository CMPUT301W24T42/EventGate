package com.example.eventgate.attendee;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.eventgate.R;
import com.example.eventgate.event.EventDB;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Adapter for Activity for attendee to view all participants of events they themselves are in
 */

public class AttendeeViewParticipantsListAdapter extends ArrayAdapter<String> {
    public AttendeeViewParticipantsListAdapter(Context context, ArrayList<String> attendees) {
        super(context, 0, attendees);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String attendee = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_attendee_view_event_participants_listview, parent, false);
        }

        TextView attendeeName = convertView.findViewById(R.id.tvAttendeeName);
        ImageView attendeePic= convertView.findViewById(R.id.ivProfilePic);

        attendeeName.setText(attendee);
        fetchImagePathAndSetImageButton(attendee, attendeePic);

        EventDB eventDB = new EventDB();

        eventDB.retrieveUserNameFromID(attendee)
                .thenAccept(name -> {
                    // Update the UI with the retrieved name
                    attendeeName.setText(name);
                })
                .exceptionally(e -> {;
                    return null;
                });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fetch user information based on the attendee ID associated with the clicked item
                FirebaseInstallations.getInstance().getId().addOnSuccessListener(installId -> {
                    new EventDB().getUserInfo(attendee).thenAccept(userInfo -> {
                        if (userInfo != null) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setMessage("Name: " + userInfo.getOrDefault("name", "") + "\n" +
                                    "Homepage: " + userInfo.getOrDefault("homepage", "") + "\n" +
                                    "Email: " + userInfo.getOrDefault("email", "") + "\n" +
                                    "Phone: " + userInfo.getOrDefault("phoneNumber", ""));
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }).exceptionally(e -> {
                        Log.e("AttendeeActivity", "Failed to fetch user info", e);
                        return null;
                    });
                });
            }
        });


        return convertView;
    }

    private void fetchImagePathAndSetImageButton(String userId, ImageView iv) {
        String pathToSearch = "attendees/" + userId + "/profilePicturePath"; // Adjusted path
        Log.d("FetchImage", "Searching for image path at: " + pathToSearch);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("attendees").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            String imagePath = documentSnapshot.getString("profilePicturePath"); // Updated key
            if (imagePath != null && !imagePath.isEmpty()) {
                Log.d("FetchImage", "Image path found: " + imagePath);

                // starts sequence of downloading and showing profile pic
                downloadImageAndSetImageButton(imagePath, iv);


                Log.d("FetchImage", "Image stored in Firestore at path: " + pathToSearch);
            } else {
                Log.d("FetchImage", "No image path found for user: " + userId);
            }
        }).addOnFailureListener(e -> {
            Log.e("FetchImage", "Error fetching image path: " + e.getMessage());
        });
    }

    /**
     * @param imagePath path of image in firebase db
     * @param imageButton profile pic imagebutton display
     */
    private void downloadImageAndSetImageButton(String imagePath, ImageView iv) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imagePath);

        // temporary file for the downloaded image
        File localFile;
        try {
            localFile = File.createTempFile("profileImage", "jpg");
        } catch (IOException e) {
            Log.e("Storage", "Error creating temporary file", e);
            return;
        }

        storageRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            iv.setImageBitmap(bitmap);
        }).addOnFailureListener(exception -> {
            Log.e("Storage", "Error downloading image", exception);
        });
    }



}