package com.example.eventgate.attendee;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventgate.MainActivity;
import com.example.eventgate.R;

public class AttendeeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee);

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
                //open file explorer, handle new image
            }
        });

        builder.setNegativeButton("Remove Profile Picture", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //remove current profile picture
            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
