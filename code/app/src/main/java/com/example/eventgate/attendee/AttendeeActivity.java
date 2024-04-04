package com.example.eventgate.attendee;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventgate.MainActivity;
import com.example.eventgate.event.Event;
import com.example.eventgate.event.EventDB;
import com.example.eventgate.R;

import com.example.eventgate.organizer.AttendeeListAdapter;
import com.example.eventgate.organizer.EventListAdapter;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import com.google.firebase.firestore.FieldValue;

import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

//citations
//https://stackoverflow.com/questions/44131469/android-using-shared-preferences-to-check-on-first-run
//https://stackoverflow.com/questions/16335178/different-font-size-of-strings-in-the-same-textview
//https://www.baeldung.com/sha-256-hashing-java

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
    Button registered_button;

    ArrayList<Event> allEventsDataList;
    ArrayAdapter<Event> allEventsAdapter;
    ListView allEventsList;
    Boolean deterministicProfileRemoved = false;
    //tracker for removing deterministic profile


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



    private static final int PICK_IMAGE = 1;
    private static byte[] hashBytes;
    private Bitmap profileBitmap;

    /**
     * Called when the activity is starting.
     * Initializes the activity layout and views.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee);


        //get saved user preferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        deterministicProfileRemoved = prefs.getBoolean("DeterministicProfileRemoved", false);


        //some handling for attendees on start

        //text sizing for events listview title
       /* TextView textView = findViewById(R.id.EventListViewTitle);
        String text = "Attendee Menu-> Your Events";
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new AbsoluteSizeSpan(13, true), 0, 13, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(20, true), 15, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);*/





        //generate user profile pic
        updateProfilePicture();

        //show profile pic
        retrieveAndSetUserImage();

        checkAndUpdateUserInfoIfNeeded();



        fetchUserIdAndSetUpListener();
        //check if first time opening attendee section, save attendee to db if so
       /* SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isFirstTimeOpening = prefs.getBoolean("isFirstTime", true);*/
        //this is all placeholder for now, im not exactly sure how we're going to handle saved user info w/ firebase auth yet
        /*if (isFirstTimeOpening) {
            user_settings_dialog();
        }*/


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




        //listener for event selection in listview
        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event clickedEvent = eventDataList.get(position);
                Intent intent = new Intent(AttendeeActivity.this, AttendeeEventViewer.class);
                intent.putExtra("EventID", clickedEvent.getEventId());
                System.out.println("eventid is:" + clickedEvent.getEventId());
                intent.putExtra("EventName", clickedEvent.getEventName());
                intent.putExtra("alerts", clickedEvent.getAlerts());
                startActivity(intent);
            }
        });


        qr_button = findViewById(R.id.qr_button);
        registered_button = findViewById(R.id.registeredEventsButton);
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

        registered_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registeredEventDialog();
            }
        });

        //view all events dialogue
        Button viewAllEventsButton = findViewById(R.id.allEventsButton);

        viewAllEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open a dialog
                viewAllEventsDialog2();
            }
        });



        //view all events dialogue
        viewAllEventsButton = findViewById(R.id.allEventsButton);

        viewAllEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open a dialog
                viewAllEventsDialog2();
            }
        });



    }




    //prepares all events popup listview on button click
    //uses eventlistadapter, probably wont use this one

    /*private void viewAllEventsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_attendeeviewallevents, null);


        allEventsDataList = new ArrayList<>();
        ListView allEventsList = dialogView.findViewById(R.id.allEventsListview);
        EventListAdapter allEventsAdapter = new EventListAdapter(this, allEventsDataList);
        allEventsList.setAdapter(allEventsAdapter);


        FirebaseInstallations.getInstance().getId().addOnSuccessListener(id -> {
            CompletableFuture<ArrayList<Event>> attendeeEvents = new EventDB().getAttendeeEvents(id);
            attendeeEvents.thenAccept(r -> {
                allEventsDataList.clear();
                allEventsDataList.addAll(r);
                allEventsAdapter.notifyDataSetChanged();
            });
        });

        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

    }*/


    //prepares all events popup listview on button click
    //uses eventlistadapter, probably wont use this one

    /*private void viewAllEventsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_attendeeviewallevents, null);


        allEventsDataList = new ArrayList<>();
        ListView allEventsList = dialogView.findViewById(R.id.allEventsListview);
        EventListAdapter allEventsAdapter = new EventListAdapter(this, allEventsDataList);
        allEventsList.setAdapter(allEventsAdapter);


        FirebaseInstallations.getInstance().getId().addOnSuccessListener(id -> {
            CompletableFuture<ArrayList<Event>> attendeeEvents = new EventDB().getAttendeeEvents(id);
            attendeeEvents.thenAccept(r -> {
                allEventsDataList.clear();
                allEventsDataList.addAll(r);
                allEventsAdapter.notifyDataSetChanged();
            });
        });

        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

    }*/

    @Override
    protected void onResume() {
        super.onResume();
        updateProfilePicture();
    }

    /**
     * runs all functions for setting deterministic profile
     * run at all times, unless pic removed
     */
    private void updateProfilePicture(){
        //generate user profile pic
        if (!deterministicProfileRemoved) {
            if (hashBytes == null || profileBitmap == null) {
                generateHash();
                createBitmap2();
                ImageButton imageButton = findViewById(R.id.profile_image);
                imageButton.setImageBitmap(profileBitmap);
            }
        }
        return;
    }


    //uses attendeelistadapter
    private void viewAllEventsDialog2() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_attendeeviewallevents, null);

        ArrayList<Event> allEvents = new ArrayList<>(); // For use in onItemClick
        ArrayList<String> allAttendeeNamesList = new ArrayList<>(); // For display in ListView
        ListView allEventsList = dialogView.findViewById(R.id.allEventsListview);
        AttendeeListAdapter allAttendeesAdapter = new AttendeeListAdapter(this, allAttendeeNamesList);
        allEventsList.setAdapter(allAttendeesAdapter);

        FirebaseInstallations.getInstance().getId().addOnSuccessListener(id -> {
            CompletableFuture<ArrayList<Event>> attendeeEvents = new EventDB().getAllEvents();
            attendeeEvents.thenAccept(events -> {
                allAttendeeNamesList.clear();
                allEvents.clear(); // Clear to ensure it's in sync with allAttendeeNamesList
                for(Event event : events) {
                    allAttendeeNamesList.add(event.getEventName());
                    allEvents.add(event); // Populate allEvents in sync with allAttendeeNamesList
                }
                allAttendeesAdapter.notifyDataSetChanged();
            });
        });

        allEventsList.setOnItemClickListener((parent, view, position, id) -> {
            Event clickedEvent = allEvents.get(position); // This should now be safe
            Intent intent = new Intent(AttendeeActivity.this, AttendeeAllEventViewerDetail.class);
            intent.putExtra("EventID", clickedEvent.getEventId());
            intent.putExtra("EventName", clickedEvent.getEventName());
            intent.putExtra("alerts", clickedEvent.getAlerts());
            startActivity(intent);
        });

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        registered_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registeredEventDialog();
            }
        });
    }

    /**
     * handles result of opening file browser and choosing profile picture image
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
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            FirebaseInstallations.getInstance().getId().addOnSuccessListener(id -> {
                uploadImageToFirebase(imageUri, id);
            }).addOnFailureListener(e -> {
                Log.e("FirebaseInstallations", "Failed to retrieve ID", e);
                Toast.makeText(getApplicationContext(), "Failed to retrieve user ID.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void uploadImageToFirebase(Uri imageUri, String userId) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference fileReference = storageRef.child("images/" + userId + ".jpg");

        Log.d("UploadImage", "Uploading image to Firebase Storage. Path: " + fileReference.getPath());

        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    saveImageReferenceInFirestore(downloadUrl, userId);


                    Log.d("UploadImage", "Image uploaded to Firebase Storage at path: " + fileReference.getPath());

                    Toast.makeText(getApplicationContext(), "Upload successful", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("UploadImage", "Failed to upload image to Firebase Storage.", e);
                });
    }

    private void saveImageReferenceInFirestore(String downloadUrl, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> profilePicture = new HashMap<>();
        profilePicture.put("profilePicturePath", downloadUrl);

        db.collection("attendees").whereEqualTo("deviceId", userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            db.collection("attendees").document(doc.getId()).update(profilePicture)
                                    .addOnSuccessListener(unused -> {
                                        Log.d("Firestore", "Profile picture path successfully written!");
                                        // show image after uploading
                                        fetchImagePathAndSetImageButton(userId, findViewById(R.id.profile_image));
                                    })
                                    .addOnFailureListener(e -> Log.w("Firestore", "Error writing document", e));
                        }
                    }
                });
    }

    private void fetchImagePathAndSetImageButton(String userId, ImageButton imageButton) {
        String pathToSearch = "attendees/" + userId + "/profilePicturePath"; // Adjusted path
        Log.d("FetchImage", "Searching for image path at: " + pathToSearch);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("attendees").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            String imagePath = documentSnapshot.getString("profilePicturePath"); // Updated key
            if (imagePath != null && !imagePath.isEmpty()) {
                Log.d("FetchImage", "Image path found: " + imagePath);

                // starts sequence of downloading and showing profile pic
                downloadImageAndSetImageButton(imagePath, imageButton);


                Log.d("FetchImage", "Image stored in Firestore at path: " + pathToSearch);
            } else {
                Log.d("FetchImage", "No image path found for user: " + userId);
            }
        }).addOnFailureListener(e -> {
            Log.e("FetchImage", "Error fetching image path: " + e.getMessage());
            Toast.makeText(getApplicationContext(), "Error fetching image path: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void downloadImageAndSetImageButton(String imagePath, ImageButton imageButton) {
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
            imageButton.setImageBitmap(bitmap);
        }).addOnFailureListener(exception -> {
            Log.e("Storage", "Error downloading image", exception);
        });
    }

    //main controller for profile pics
    public void retrieveAndSetUserImage() {
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(installId -> {
            String userId = installId;
            ImageButton imageButton = findViewById(R.id.profile_image);
            fetchImagePathAndSetImageButton(userId, imageButton);
        }).addOnFailureListener(e -> {
            Log.e("FirebaseInstallations", "Error retrieving Firebase Install ID", e);
        });
    }


    /**
     * Popup dialog for editing user settings when gear icon clicked
     */
    private void user_settings_dialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.user_settings_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(AttendeeActivity.this);
        builder.setView(customView);

        EditText editTextName = customView.findViewById(R.id.edittext_name);
        EditText editTextHomepage = customView.findViewById(R.id.edittext_user_homepage);
        EditText editTextEmail = customView.findViewById(R.id.edittext_user_email);
        EditText editTextPhone = customView.findViewById(R.id.edittextPhone);
        CheckBox checkboxGeolocation = customView.findViewById(R.id.checkbox_geolocation);

        // Retrieve existing user info and populate fields
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(installId -> {
            new EventDB().getUserInfo(installId).thenAccept(userInfo -> {
                if (userInfo != null) {
                    editTextName.setText(userInfo.getOrDefault("name", "").toString());
                    editTextHomepage.setText(userInfo.getOrDefault("homepage", "").toString());
                    editTextEmail.setText(userInfo.getOrDefault("email", "").toString());
                    editTextPhone.setText(userInfo.getOrDefault("phoneNumber", "").toString());
                }
            }).exceptionally(e -> {
                Log.e("AttendeeActivity", "Failed to fetch user info", e);
                return null;
            });
        });

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = editTextName.getText().toString();
            String homepage = editTextHomepage.getText().toString();
            String email = editTextEmail.getText().toString();
            String phone = editTextPhone.getText().toString();
            boolean isGeolocationEnabled = checkboxGeolocation.isChecked();

            // must have name
            if (!name.isEmpty()) {
                FirebaseInstallations.getInstance().getId().addOnSuccessListener(installId -> {
                    new EventDB().updateUserInfo(installId, name, phone, email, homepage, true)
                            .thenRun(() -> Log.d("AttendeeActivity", "User info updated successfully"))
                            .exceptionally(e -> {
                                Log.e("AttendeeActivity", "Failed to update user info", e);
                                return null;
                            });
                });
            } else {
                Toast.makeText(AttendeeActivity.this, "Name required.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Popup for editing/removing profile picture, called when profile image clicked
     */
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
                imageButton.setImageResource(R.drawable.profile_picture);
                // Save that deterministic profile pic has been removed to sharedpref
                getSharedPreferences("MyAppPrefs", MODE_PRIVATE).edit()
                        .putBoolean("DeterministicProfileRemoved", true)
                        .apply();

                // Clear the profile picture path in Firestore db
                FirebaseInstallations.getInstance().getId().addOnSuccessListener(installId -> {
                    String userId = installId;
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("attendees").document(userId)
                            .update("profilePicturePath", null)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("RemoveProfilePic", "Profile picture removed successfully!");
                                Toast.makeText(getApplicationContext(), "Profile picture removed successfully!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("RemoveProfilePic", "Error removing profile picture", e);

                            });
                }).addOnFailureListener(e -> {
                    Log.e("FirebaseInstallations", "Error retrieving Firebase Install ID", e);

                });

                getSharedPreferences("MyAppPrefs", MODE_PRIVATE).edit()
                        .putBoolean("DeterministicProfileRemoved", true)
                        .apply();

            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //different user info popup for first time attendee, not implemented yet
    private void user_settings_dialog_first() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.user_settings_dialog, null);


        AlertDialog.Builder builder = new AlertDialog.Builder(AttendeeActivity.this);
        builder.setView(customView);


        EditText editTextName = customView.findViewById(R.id.edittext_name);
        EditText editTextHomepage = customView.findViewById(R.id.edittext_user_homepage);
        EditText editTextEmail = customView.findViewById(R.id.edittext_user_email);
        CheckBox checkboxGeolocation = customView.findViewById(R.id.checkbox_geolocation);


        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //maybe try to retrieve known user info if not first time opening
                String name = editTextName.getText().toString();
                String user_homepage = editTextHomepage.getText().toString();
                String contactInfo = editTextEmail.getText().toString();
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


    /**
     * generates hash from firebase auth id for deterministic profile picture
     */
    //
    private void generateHash() {
        try {
            FirebaseUser currentUser = MainActivity.db.getmAuth().getCurrentUser();
            String input = currentUser.getUid();
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
        } catch (NullPointerException e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * Generates deterministic profile picture for attendee
     */
    //better version
    private void createBitmap2() {
        if (hashBytes == null || hashBytes.length < 3) {
            return;
        }

        int color = Color.rgb(hashBytes[0] & 0xFF, hashBytes[1] & 0xFF, hashBytes[2] & 0xFF);
        profileBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(profileBitmap);
        canvas.drawColor(color);


        int blockSize = 5;
        int currentIndex = 0;

        for (int y = 0; y < 200; y += blockSize) {
            for (int x = 0; x < 200; x += blockSize) {
                int blockColor = Color.rgb(hashBytes[currentIndex] & 0xFF, hashBytes[(currentIndex + 1) % hashBytes.length] & 0xFF, hashBytes[(currentIndex + 2) % hashBytes.length] & 0xFF);
                Paint paint = new Paint();
                paint.setColor(blockColor);
                int endX = Math.min(x + blockSize, 200);
                int endY = Math.min(y + blockSize, 200);
                canvas.drawRect(x, y, endX, endY, paint);
                currentIndex = (currentIndex + 3) % hashBytes.length;
            }
        }
    }

    /**
     * Queries firestore db for all events attendee is signed into
     */
    private void updateMyEvents() {
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(id -> {
            CompletableFuture<ArrayList<Event>> attendeeEvents = new EventDB().getAttendeeEvents(id);
            attendeeEvents.thenAccept(r -> {
                eventDataList.clear();
                eventDataList.addAll(r);
                eventAdapter.notifyDataSetChanged();

            });
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Popup dialog for editing user settings when gear icon clicked
     */
    private void registeredEventDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.attendee_registered_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(AttendeeActivity.this);
        builder.setView(customView);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ArrayList<Event> eventRegisteredDataList = new ArrayList<>();
        ListView eventRegisteredList = customView.findViewById(R.id.event_registered_list);
        ArrayAdapter<Event> eventRegisteredAdapter = new AttendeeEventListAdapter(this,
                eventRegisteredDataList);
        eventRegisteredList.setAdapter(eventRegisteredAdapter);
        updateMyRegisteredEvents(eventRegisteredDataList, eventRegisteredAdapter);

        eventRegisteredList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event clickedEvent = eventRegisteredDataList.get(position);
                Intent intent = new Intent(AttendeeActivity.this, AttendeeEventViewer.class);
                intent.putExtra("EventID", clickedEvent.getEventId());
                intent.putExtra("EventName", clickedEvent.getEventName());
                intent.putExtra("alerts", clickedEvent.getAlerts());
                startActivity(intent);
            }
        });

        alertDialog.show();
    }

    /**
     * Update a user's registered events
     * @param dataList The list to append events to
     * @param adapter The adapter to update
     */
    private void updateMyRegisteredEvents(ArrayList<Event> dataList, ArrayAdapter<Event> adapter) {
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(id -> {
            CompletableFuture<ArrayList<Event>> attendeeEvents = new EventDB().getRegisteredEvents(id);
            attendeeEvents.thenAccept(r -> {
                dataList.clear();
                dataList.addAll(r);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void checkAndUpdateUserInfoIfNeeded() {
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(installId -> {
            new EventDB().getUserInfoUpdateStatus(installId).thenAccept(hasUpdatedInfo -> {
                // hasUpdatedInfo is null or false, show the dialog
                if (hasUpdatedInfo == null || Boolean.FALSE.equals(hasUpdatedInfo)) {
                    user_settings_dialog();
                }
            }).exceptionally(e -> {
                Log.e("AttendeeActivity", "Error checking user info update status", e);
                return null;
            });
        }).addOnFailureListener(e -> {
            Log.e("AttendeeActivity", "Error getting Firebase Install ID", e);
        });
    }

    /*private void updateUserInfoInView() {
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(installId -> {
            new EventDB().getUserInfo(installId).thenAccept(userInfo -> {
                if (userInfo != null) {

                    TextView userNameTextView = findViewById(R.id.user_name);
                    TextView userPhoneTextView = findViewById(R.id.user_phone);
                    TextView userEmailTextView = findViewById(R.id.user_email);
                    TextView userHomepageTextView = findViewById(R.id.user_homepage);
                    runOnUiThread(() -> {
                        userNameTextView.setText((String) userInfo.getOrDefault("name", "No Name"));
                        userPhoneTextView.setText((String) userInfo.getOrDefault("phoneNumber", "No Phone Number"));
                        userEmailTextView.setText((String) userInfo.getOrDefault("email", "No Email"));
                        userHomepageTextView.setText((String) userInfo.getOrDefault("homepage", "No Homepage"));
                    });
                }
            }).exceptionally(e -> {
                Log.e("out", "Error updating user info view", e);
                return null;
            });
        });
    }*/

    private void fetchUserIdAndSetUpListener() {
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(userId -> {
            if (userId != null) {
                setUpUserInfoListener(userId);
            }
        }).addOnFailureListener(e -> Log.e("out", "Error fetching fid", e));
    }


    private void setUpUserInfoListener(String userId) {
        // Assuming EventDB.getUserInfo returns CompletableFuture<Map<String, Object>>
        new EventDB().getUserInfo(userId).thenAccept(userInfo -> {
            if (userInfo != null) {
                updateUIWithUserInfo(userInfo);
            }
        }).exceptionally(e -> {
            Log.e("AttendeeActivity", "Error fetching user info", e);
            return null;
        });
    }

    private void updateUIWithUserInfo(Map<String, Object> userInfo) {
        TextView userNameTextView = findViewById(R.id.user_name);
        TextView userPhoneTextView = findViewById(R.id.user_phone);
        TextView userEmailTextView = findViewById(R.id.user_email);
        TextView userHomepageTextView = findViewById(R.id.user_homepage);
        runOnUiThread(() -> {
            userNameTextView.setText((String) userInfo.getOrDefault("name", "No Name"));
            userPhoneTextView.setText((String) userInfo.getOrDefault("phoneNumber", "No Phone Number"));
            userEmailTextView.setText((String) userInfo.getOrDefault("email", "No Email"));
            userHomepageTextView.setText((String) userInfo.getOrDefault("homepage", "No Homepage"));
        });
    }


}