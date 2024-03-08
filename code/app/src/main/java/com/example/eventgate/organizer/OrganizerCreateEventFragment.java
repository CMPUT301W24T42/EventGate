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
import com.example.eventgate.event.EventDB;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

/**
 * A fragment for creating a new event.
 * This fragment provides functionality for organizers to create new events by generating QR codes for check-in and event description.
 * It communicates with the parent activity to handle event addition and updates the UI accordingly.
 * Outstanding issues: When a description QR Code is generated, it is not being added to the database.
 */
public class OrganizerCreateEventFragment extends DialogFragment {
    private Boolean qRCodeGenerated = false;
    private Boolean descriptionQRCodeGenerated = false;
    private Bitmap eventQRBitmap;
    private Bitmap descriptionQRBitmap;
    private OnQRCodeGeneratedListener qrCodeListener;
    private Event eventAdded;
    private String eventName;

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
        ImageView checkInQRCode = view.findViewById(R.id.organizerCreateEventQRCode);
        ImageView descriptionQRCode = view.findViewById(R.id.organizerEventDescriptionQRCode);
        Button generateQRButton = view.findViewById(R.id.generateQRButton);
        Button generateDescriptionQRButton = view.findViewById(R.id.generateDescriptionQRButton);

        EditText eventDetailsEditText = view.findViewById(R.id.eventDetailsEdittext);


        // This QR Code is for attendees to check in to the event
        generateQRButton.setOnClickListener(v -> {
            // Only generate one QR Code
            if (!qRCodeGenerated) {
                eventName = organizerCreateEventName.getText().toString().trim();

                // Check if the eventName is empty or null
                if (eventName.isEmpty()) {
                    // Show a message to the user indicating that they need to enter an event name
                    Toast.makeText(getActivity(), "Please enter an Event name. Every QR Code must be associated with an Event name.", Toast.LENGTH_SHORT).show();
                    return; // Exit the method
                }

                // If the Description QR code has not been generated, then an event must be created
                if (!descriptionQRCodeGenerated) {
                    // Create an Event object
                    eventAdded = new Event(eventName);
                }

                if (qrCodeListener != null) {
                    qrCodeListener.onQRCodeGenerated(eventQRBitmap);
                }

                if (getActivity() instanceof OnEventAddedListener) {
                    // Pass the event name and listener to handle the QR code bitmap
                    ((OnEventAddedListener) getActivity()).onEventAdded(eventAdded, eventQRBitmap -> {
                        checkInQRCode.setImageBitmap(eventQRBitmap);
                        qRCodeGenerated = true;
                    });
                }
            }
        });

        // This QR Code is for attendees to view the Event details
        generateDescriptionQRButton.setOnClickListener(v -> {
            // Only create 1 QR Code
            if (!descriptionQRCodeGenerated) {
                eventName = organizerCreateEventName.getText().toString().trim();

                if (eventName.isEmpty()) {
                    // Show a message to the user indicating that they need to enter an event name
                    Toast.makeText(getActivity(), "Please enter an Event name. Every QR Code must be associated with an Event name.", Toast.LENGTH_SHORT).show();
                    return; // Exit the method
                }

                // Create an Event object
                eventAdded = new Event(eventName);
                String eventDetails = eventDetailsEditText.getText().toString();
                eventAdded.setEventDetails(eventDetails);

                // Create Event Description QR Code
                MultiFormatWriter writer = new MultiFormatWriter();

                try {
                    BitMatrix matrix = writer.encode(eventAdded.getEventName(), BarcodeFormat.QR_CODE, 400, 400);
                    BarcodeEncoder encoder = new BarcodeEncoder();
                    descriptionQRBitmap = encoder.createBitmap(matrix);

                } catch (WriterException e) {
                    e.printStackTrace();
                }

                eventAdded.setEventQRBitmap(eventQRBitmap);
                descriptionQRCode.setImageBitmap(descriptionQRBitmap);
                descriptionQRCodeGenerated = true;
            }
        });

        continueButton.setOnClickListener(v -> {
            String eventName = organizerCreateEventName.getText().toString().trim();

            // Check if the QR code has been generated
            if (!qRCodeGenerated) {
                // Show a message to the user indicating that they need to generate a QR code
                Toast.makeText(getActivity(), "Please generate both QR Codes. An Event must be associated with both QR Codes.", Toast.LENGTH_SHORT).show();
                return; // Exit the method
            }

            if (!descriptionQRCodeGenerated) {
                // Show a message to the user indicating that they need to generate a QR code
                Toast.makeText(getActivity(), "Please generate both QR Codes. An Event must be associated with both QR Codes.", Toast.LENGTH_SHORT).show();
                return; // Exit the method
            }

            if (!eventName.isEmpty()) { // Check if the event name is not empty
                dismiss(); // Close the dialog
            } else {
                // Show a toast message indicating that the event name cannot be empty
                Toast.makeText(getActivity(), "Please enter a valid event name", Toast.LENGTH_SHORT).show();
            }
        });


        cancelButton.setOnClickListener(v -> {
            if (eventAdded != null) {
                EventDB eventDB = new EventDB();


                eventDB.removeEvent(eventAdded);

                // Update the event list in the main activity
                if (getActivity() instanceof OrganizerMainMenuActivity) {
                    ((OrganizerMainMenuActivity) getActivity()).updateOrganizerEvents();
                }
            }
            dismiss();
        });

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view); // Set the custom layout
        return builder.create();
    }
}
