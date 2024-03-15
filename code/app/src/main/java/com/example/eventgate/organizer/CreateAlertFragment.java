package com.example.eventgate.organizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eventgate.R;

/**
 * A fragment for creating a new alert.
 * This fragment provides functionality for organizers to create new alerts by entering a message
 */
public class CreateAlertFragment extends DialogFragment {
    /**
     * Interface definition for a callback to be invoked when an alert is added.
     * Implementing classes must define the behavior to be executed when an alert is added.
     */
    public interface OnAlertCreatedListener {
        void onAlertCreated(OrganizerAlert alert);
    }

    /**
     * Called to create the dialog shown in this fragment.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     * @return A new Dialog instance to be displayed by the fragment.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.create_alert_popup, null);

        EditText titleEditText = view.findViewById(R.id.title_edit_text);
        EditText messageEditText = view.findViewById(R.id.message_edit_text);
        Button sendButton = view.findViewById(R.id.create_alert_send_button);
        Button cancelButton = view.findViewById(R.id.create_alert_cancel_button);

        // takes the inputted message and creates an OrganizerAlert object with it
        sendButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String message = messageEditText.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter a title", Toast.LENGTH_SHORT).show();
            } else if (message.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter a message", Toast.LENGTH_SHORT).show();
            } else {
                OrganizerAlert alert = new OrganizerAlert(title, message, "event_channel", "event");
                ((CreateAlertFragment.OnAlertCreatedListener) getActivity()).onAlertCreated(alert);
                dismiss();
            }
        });

        // dismisses dialog if user does not want to create an alert
        cancelButton.setOnClickListener(v -> dismiss());

        // build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view); // Set the custom layout
        return builder.create();
    }
}
