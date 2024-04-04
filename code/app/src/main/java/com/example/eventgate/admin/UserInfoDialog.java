package com.example.eventgate.admin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eventgate.R;
import com.example.eventgate.attendee.Attendee;
import com.example.eventgate.attendee.AttendeeDB;

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

        Bundle args = getArguments();
        if (args != null) {
            attendee = (Attendee) args.getSerializable("attendee");
            name = attendee.getName();
            homepage = attendee.getHomepage();
            email = attendee.getEmail();
            phoneNumber = attendee.getPhoneNumber();

            TextView attendeeName = view.findViewById(R.id.attendee_name);
            TextView attendeeHomepage = view.findViewById(R.id.attendee_homepage);
            TextView attendeeEmail = view.findViewById(R.id.attendee_email);
            TextView attendeeNumber = view.findViewById(R.id.attendee_number);

            attendeeName.setText(name);
            attendeeHomepage.setText(homepage);
            attendeeEmail.setText(email);
            attendeeNumber.setText(phoneNumber);

        }

        Button dismissButton = view.findViewById(R.id.user_info_dismiss_button);
        dismissButton.setOnClickListener(v -> dismiss());

        Button deleteButton = view.findViewById(R.id.delete_picture_button);
        // set on click listener

        // build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view); // Set the custom layout
        return builder.create();
    }
}
