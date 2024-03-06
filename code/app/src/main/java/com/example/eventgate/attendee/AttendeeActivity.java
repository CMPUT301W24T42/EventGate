package com.example.eventgate.attendee;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventgate.MainActivity;
import com.example.eventgate.R;

//citations
//https://stackoverflow.com/questions/44131469/android-using-shared-preferences-to-check-on-first-run
//https://stackoverflow.com/questions/16335178/different-font-size-of-strings-in-the-same-textview


public class AttendeeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee);

        //some UI adjustments

        //text sizing for events listview title
        TextView textView = findViewById(R.id.EventListViewTitle);
        String text = "Attendee Menu-> Your Events";
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new AbsoluteSizeSpan(13, true), 0, 13, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(20, true), 15, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);


        //check if first time opening attendee section, save attendee to db if so
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isFirstTimeOpening = prefs.getBoolean("isFirstTime", true);

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
}
