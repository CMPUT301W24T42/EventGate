package com.example.eventgate.attendee;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventgate.MainActivity;
import com.example.eventgate.event.Event;
import com.example.eventgate.event.EventDB;
import com.example.eventgate.MainActivity;
import com.example.eventgate.R;
import com.example.eventgate.admin.AdminEventListAdapter;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.installations.FirebaseInstallations;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import java.security.MessageDigest;

//citations
//https://stackoverflow.com/questions/44131469/android-using-shared-preferences-to-check-on-first-run
//https://stackoverflow.com/questions/16335178/different-font-size-of-strings-in-the-same-textview
//https://www.baeldung.com/sha-256-hashing-java

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * This is the activity for the attendee main menu.
 * It allows the attendee to view checked-in events and to check-into events
 */
public class AttendeeActivity extends AppCompatActivity {
    public static final int RESULT_NOT_FOUND = 2;
    private static final int RESULT_REDUNDANT = 3;

    ArrayList<Event> eventDataList;
    ListView eventList;
    ArrayAdapter<Event> eventAdapter;
    Button qr_button;
    Button back_button;

    private final ActivityResultLauncher<Intent> qrLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        updateMyEvents();
                    } else if (result.getResultCode() == RESULT_NOT_FOUND) {
                        showToast("Event Not Found");
                    } else if (result.getResultCode() == RESULT_REDUNDANT) {
                        showToast("Already Checked In!");
                    }
                });

    /**
     * Called when the activity is starting.
     * Initializes the activity layout and views.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */

    private static final int PICK_IMAGE = 1;
    private static byte[] hashBytes;
    private Bitmap profileBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee);

        //some handling for attendees on start

        //text sizing for events listview title
       /* TextView textView = findViewById(R.id.EventListViewTitle);
        String text = "Attendee Menu-> Your Events";
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new AbsoluteSizeSpan(13, true), 0, 13, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(20, true), 15, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);*/


        //generate user profile pic
        if (hashBytes == null) {
            generateHash();
            createBitmap2();
            ImageButton imageButton = findViewById(R.id.profile_image);
            imageButton.setImageBitmap(profileBitmap);
        }


        //check if first time opening attendee section, save attendee to db if so
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isFirstTimeOpening = prefs.getBoolean("isFirstTime", true);
        //this is all placeholder for now, im not exactly sure how we're going to handle saved user info w/ firebase auth yet
        if (isFirstTimeOpening) {
            user_settings_dialog();

        }


        //buttons for user settings and profile pic settings
        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_settings_dialog();
            }
        });

        ImageButton profileImageButton = findViewById(R.id.profile_image);
        profileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfilePictureDialog();
            }
        });

        eventDataList = new ArrayList<>();
        eventList = findViewById(R.id.event_list);
        eventAdapter = new AttendeeEventListAdapter(this, eventDataList);
        eventList.setAdapter(eventAdapter);

        updateMyEvents();

        qr_button = findViewById(R.id.qr_button);

        back_button = findViewById(R.id.attendee_back_button);

        qr_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeActivity.this, QRCodeScanActivity.class);
                qrLauncher.launch(intent);
            }
        });

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            if (data == null) {
                // Display an error
                return;
            }
            try {
                ImageButton imageButton = findViewById(R.id.profile_image);
                Uri imageUri = data.getData();
                imageButton.setImageURI(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Popup for user settings when gear icon clicked
    private void user_settings_dialog() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.user_settings_dialog, null);


        AlertDialog.Builder builder = new AlertDialog.Builder(AttendeeActivity.this);
        builder.setView(customView);


        EditText editTextName = customView.findViewById(R.id.edittext_name);
        EditText editTextHomepage = customView.findViewById(R.id.edittext_homepage);
        EditText editTextContactInfo = customView.findViewById(R.id.edittext_contact_info);
        CheckBox checkboxGeolocation = customView.findViewById(R.id.checkbox_geolocation);


        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //maybe try to retrieve known user info if not first time opening
                String name = editTextName.getText().toString();
                String homepage = editTextHomepage.getText().toString();
                String contactInfo = editTextContactInfo.getText().toString();
                boolean isGeolocationEnabled = checkboxGeolocation.isChecked();

                // Upload info to firebase
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Popup for editing/removing profile picture, called when profile image clicked
    private void editProfilePictureDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Profile Picture");


        builder.setPositiveButton("Upload New Profile Picture", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        builder.setNegativeButton("Remove Profile Picture", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //remove current profile picture
                ImageButton imageButton = findViewById(R.id.profile_image);
                imageButton.setImageDrawable(null);
            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //different user info popup for first time attendee
    private void user_settings_dialog_first() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.user_settings_dialog, null);


        AlertDialog.Builder builder = new AlertDialog.Builder(AttendeeActivity.this);
        builder.setView(customView);


        EditText editTextName = customView.findViewById(R.id.edittext_name);
        EditText editTextHomepage = customView.findViewById(R.id.edittext_homepage);
        EditText editTextContactInfo = customView.findViewById(R.id.edittext_contact_info);
        CheckBox checkboxGeolocation = customView.findViewById(R.id.checkbox_geolocation);


        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //maybe try to retrieve known user info if not first time opening
                String name = editTextName.getText().toString();
                String homepage = editTextHomepage.getText().toString();
                String contactInfo = editTextContactInfo.getText().toString();
                boolean isGeolocationEnabled = checkboxGeolocation.isChecked();

                // Upload info to firebase
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    //generates hash from firebase auth id
    private void generateHash() {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String userId = currentUser.getUid();
            String input = userId;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            String userHash = hexString.toString();
            hashBytes = userHash.getBytes("UTF-8");
            return;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }


    }
    //generates profile picture from hash
    private void createBitMap() {

        int color = Color.rgb(hashBytes[0] & 0xFF, hashBytes[1] & 0xFF, hashBytes[2] & 0xFF);
        profileBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(profileBitmap);
        canvas.drawColor(color);

        return;
    }
    //better version
    private void createBitmap2() {
        if (hashBytes == null || hashBytes.length < 3) {
            return; // Return if hashBytes is null or has insufficient length
        }

        int color = Color.rgb(hashBytes[0] & 0xFF, hashBytes[1] & 0xFF, hashBytes[2] & 0xFF);
        profileBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(profileBitmap);
        canvas.drawColor(color);

        // Draw the abstract pattern based on hash
        int blockSize = 10; // Size of each block
        int currentIndex = 0; // Index to keep track of the hash value being used

        for (int y = 0; y < 100; y += blockSize) {
            for (int x = 0; x < 100; x += blockSize) {
                // Loop through the hash bytes and use each byte to set a color
                int blockColor = Color.rgb(hashBytes[currentIndex] & 0xFF, hashBytes[(currentIndex + 1) % hashBytes.length] & 0xFF, hashBytes[(currentIndex + 2) % hashBytes.length] & 0xFF);
                Paint paint = new Paint();
                paint.setColor(blockColor);
                canvas.drawRect(x, y, x + blockSize, y + blockSize, paint);
                currentIndex = (currentIndex + 3) % hashBytes.length;
            }
        }
    }
    private void updateMyEvents() {
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(id -> {
            CompletableFuture<ArrayList<Event>> attendeeEvents = new EventDB().getAttendeeEvents(id);
            attendeeEvents.thenAccept(r -> {
                eventDataList.clear();
                for (Event e : r) {
                    eventDataList.add(0, e);
                }
                eventAdapter.notifyDataSetChanged();
            });
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }






}