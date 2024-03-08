package com.example.eventgate.organizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
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
import com.example.eventgate.event.Event;

public class CreateAlertFragment extends DialogFragment {
    public interface OnAlertCreatedListener {
        void onAlertCreated(OrganizerAlert alert);
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.create_alert_popup, null);

        EditText messageEditText = view.findViewById(R.id.message_edit_text);
        Button sendButton = view.findViewById(R.id.create_alert_send_button);
        Button cancelButton = view.findViewById(R.id.create_alert_cancel_button);

        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter a message", Toast.LENGTH_SHORT).show();
            } else {
                OrganizerAlert alert = new OrganizerAlert(message);
                ((CreateAlertFragment.OnAlertCreatedListener) getActivity()).onAlertCreated(alert);
                dismiss();
            }
        });

        cancelButton.setOnClickListener(v -> dismiss());

        // build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view); // Set the custom layout
        return builder.create();
    }
}
