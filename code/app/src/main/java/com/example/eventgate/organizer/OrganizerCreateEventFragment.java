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
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.eventgate.event.Event;
import com.example.eventgate.R;

/**
 * A fragment for creating a new event.
 */
public class OrganizerCreateEventFragment extends DialogFragment {
    private Boolean qRCodeGenerated = false;
    private Bitmap eventQRBitmap;
    private OnQRCodeGeneratedListener qrCodeListener;

    /**
     * Interface definition for a callback to be invoked when an event is added.
     * Implementing classes must define the behavior to be executed when an event is added.
     */
    public interface OnEventAddedListener {
        Bitmap onEventAdded(Event event, OnQRCodeGeneratedListener listener);
    }

    /**
     * Interface definition for a callback to be invoked when a QR code bitmap is generated.
     * Implementing classes must define the behavior to be executed when a QR code bitmap is generated.
     */
    public interface OnQRCodeGeneratedListener {
        void onQRCodeGenerated(Bitmap qrBitmap);
    }

    /**
     * Sets the event added listener for this fragment.
     *
     * @param listener      The listener to be set.
     * @param qrCodeListener The listener for QR code generation.
     */
    public void setOnEventAddedListener(OnEventAddedListener listener, OnQRCodeGeneratedListener qrCodeListener) {
    }

    /**
     * Sets the QR code generated listener for this fragment.
     *
     * @param listener The listener to be set.
     */
    public void setOnQRCodeGeneratedListener(OnQRCodeGeneratedListener listener) {
        this.qrCodeListener = listener;
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
        Button generateQRButton = view.findViewById(R.id.generateQRButton);

        generateQRButton.setOnClickListener(v -> {
            // Only generate one QR Code
            if (!qRCodeGenerated) {
                String eventName = organizerCreateEventName.getText().toString().trim();

                if (qrCodeListener != null) {
                    qrCodeListener.onQRCodeGenerated(eventQRBitmap);
                }

                // Check if the eventName is empty or null
                if (eventName.isEmpty()) {
                    // Show a message to the user indicating that they need to enter an event name
                    Toast.makeText(getActivity(), "Please enter an Event name. A QR Code must be associated with an Event name.", Toast.LENGTH_SHORT).show();
                    return; // Exit the method
                }

                // Create an Event object
                Event event = new Event(eventName);

                if (getActivity() instanceof OnEventAddedListener) {
                    // Pass the event name and listener to handle the QR code bitmap
                    ((OnEventAddedListener) getActivity()).onEventAdded(event, eventQRBitmap -> {
                        qRCode.setImageBitmap(eventQRBitmap);
                        qRCodeGenerated = true;
                    });
                }
            }
        });

        // Set up behavior for continue button
        continueButton.setOnClickListener(v -> {
            // Handle continue button click
            String eventName = organizerCreateEventName.getText().toString().trim();

            // Check if the QR code has been generated
            if (!qRCodeGenerated) {
                // Show a message to the user indicating that they need to generate a QR code
                Toast.makeText(getActivity(), "Please generate a QR Code. An Event must be associated with a QR Code.", Toast.LENGTH_SHORT).show();
                return; // Exit the method
            }

            if (!eventName.isEmpty()) { // Check if the event name is not empty
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
