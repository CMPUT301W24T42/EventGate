package com.example.eventgate.organizer;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.eventgate.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

/**
 * A fragment for creating a new event.
 */
public class OrganizerCreateEventFragment extends DialogFragment {
    /**
     * Interface definition for a callback to be invoked when an event is added.
     */
    public interface OnEventAddedListener {
        void onEventAdded(String eventName);
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
        // Inflate the layout for the dialog
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.organizer_create_event_popup, null);

        Button continueButton = view.findViewById(R.id.organizerCreateEventContinueButton);
        Button cancelButton = view.findViewById(R.id.organizerCreateEventCancelButton);
        EditText organizerCreateEventName = view.findViewById(R.id.organizerCreateEventName);
        ImageView qRCode = view.findViewById(R.id.organizerCreateEventQRCode);

        // Set up behavior for continue button
        continueButton.setOnClickListener(v -> {
            // Handle continue button click
            String eventName = organizerCreateEventName.getText().toString().trim(); // Get the entered event name
            MultiFormatWriter writer = new MultiFormatWriter();

            try {
                BitMatrix matrix = writer.encode(eventName, BarcodeFormat.QR_CODE, 400, 400);

                BarcodeEncoder encoder = new BarcodeEncoder();
                Bitmap bitmap = encoder.createBitmap(matrix);
                qRCode.setImageBitmap(bitmap);

            } catch (WriterException e) {
                e.printStackTrace();
            }

            if (!eventName.isEmpty()) { // Check if the event name is not empty
                if (getActivity() instanceof OnEventAddedListener) {
                    ((OnEventAddedListener) getActivity()).onEventAdded(eventName); // Pass the event name to the activity
                }
                dismiss(); // Close the dialog
            } else {
                // Show a toast message indicating that the event name cannot be empty
                Toast.makeText(getActivity(), "Please enter a valid event name", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up behavior for cancel button
        cancelButton.setOnClickListener(v -> {
            // Handle cancel button click
            dismiss(); // Close the dialog
        });

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view); // Set the custom layout
        return builder.create();
    }
}
