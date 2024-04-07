package com.example.eventgate.admin;

import static com.example.eventgate.admin.DeleteImageFromFirebase.deleteProfilePicFromCloudStorage;
import static com.example.eventgate.admin.DeleteImageFromFirebase.deleteProfilePicFromFirestore;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eventgate.R;
import com.example.eventgate.attendee.Attendee;
import com.squareup.picasso.Picasso;

/**
 * Dialog that allows the admin to see a user's profile in detail, including their profile picture,
 *      homepage, email, and phone number. It also allows the admin to delete their profile picture
 */
public class UserInfoDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.user_info_popup, null);

        Attendee attendee;
        String name;
        String homepage;
        String email;
        String phoneNumber;
        String profilePicture;
        Button dismissButton = view.findViewById(R.id.user_info_dismiss_button);
        Button deleteButton = view.findViewById(R.id.delete_picture_button);
        TextView deleteMessage = view.findViewById(R.id.delete_message);

        // get data from bundle
        Bundle args = getArguments();
        if (args != null) {
            attendee = (Attendee) args.getSerializable("attendee");
            name = attendee.getName();
            homepage = attendee.getHomepage();
            email = attendee.getEmail();
            phoneNumber = attendee.getPhoneNumber();
            profilePicture = attendee.getProfilePicture();

            // find references to views
            ImageView attendeePicture = view.findViewById(R.id.profile_picture);
            TextView attendeeName = view.findViewById(R.id.attendee_name);
            TextView attendeeHomepage = view.findViewById(R.id.attendee_homepage);
            TextView attendeeEmail = view.findViewById(R.id.attendee_email);
            TextView attendeeNumber = view.findViewById(R.id.attendee_number);

            // sets the user's profile picture, unless they don't have one, then it sets a default profile picture
            if (profilePicture == null || profilePicture.isEmpty()) {
                attendeePicture.setImageResource(R.drawable.profile_picture);
                // if the user doesn't have a profile picture, take away the option to delete the profile picture
                deleteMessage.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
            } else {  // profile picture exists, then load it into the imageview
                Picasso.get().load(profilePicture).fit().centerCrop().into(attendeePicture);
            }

            // set attendee info into textviews
            attendeeName.setText(name);
            attendeeHomepage.setText(homepage);
            attendeeEmail.setText(email);
            attendeeNumber.setText(phoneNumber);

            // set on delete click listener
            deleteButton.setOnClickListener(v -> {
                // once the profile picture is deleted, then the imageview should be set to the default
                //      profile picture and the option to delete the profile picture should be taken away
                //      (button made unclickable)
                attendeePicture.setImageResource(R.drawable.profile_picture);
                deleteButton.setClickable(false);
                deleteButton.setBackgroundColor(getResources().getColor(R.color.light_gray));
                deleteButton.setTextColor(getResources().getColor(R.color.dark_gray));
                // delete the profile picture from firebase
                deleteProfilePicFromFirestore(attendee.getAttendeeId(), profilePicture);
                deleteProfilePicFromCloudStorage(attendee.getDeviceId());
            });
        }

        dismissButton.setOnClickListener(v -> dismiss());

        // build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view); // Set the custom layout
        return builder.create();
    }
}
