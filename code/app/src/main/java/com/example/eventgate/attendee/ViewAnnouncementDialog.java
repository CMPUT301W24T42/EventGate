package com.example.eventgate.attendee;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eventgate.R;

/**
 * this is a dialog that shows the user the full message of an announcement, in addition to the title
 */
public class ViewAnnouncementDialog extends DialogFragment {
    String title;
    String message;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // inflate view
        View view = getLayoutInflater().inflate(R.layout.view_announcement_popup, null);

        // get info from bundle
        Bundle args = getArguments();
        if (args != null) {
            title = args.getString("alertTitle");
            message = args.getString("alertMessage");
        }

        // find references to views
        TextView alertTitle = view.findViewById(R.id.announcement_title);
        TextView alertMessage = view.findViewById(R.id.announcement_message);
        Button dismissButton = view.findViewById(R.id.announcement_popup_dismiss_button);

        // set info in view
        alertTitle.setText(title);
        alertMessage.setText(message);

        // dismiss dialog
        dismissButton.setOnClickListener(v -> dismiss());

        // build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view); // Set the custom layout
        return builder.create();
    }
}
