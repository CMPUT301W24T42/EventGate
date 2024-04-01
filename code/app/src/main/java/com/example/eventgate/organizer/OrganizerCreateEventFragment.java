package com.example.eventgate.organizer;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.eventgate.event.Event;
import com.example.eventgate.R;
import com.example.eventgate.event.EventDB;


/**
 * A fragment for creating a new event.
 * This fragment provides functionality for organizers to create new events by generating QR codes for check-in and event description.
 * It communicates with the parent activity to handle event addition and updates the UI accordingly.
 * Outstanding issues: When a description QR Code is generated, it is not being added to the database.
 */
public class OrganizerCreateEventFragment extends DialogFragment {
    private Event eventAdded;

    /**
     * Interface definition for a callback to be invoked when an event is added.
     * Implementing classes must define the behavior to be executed when an event is added.
     */
    public interface OnEventAddedListener {
        void onEventAdded(Event event);
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
        EditText eventDetailsEditText = view.findViewById(R.id.eventDetailsEdittext);
        EditText organizerLimitAttendance = view.findViewById(R.id.organizerLimitAttendance);

        continueButton.setOnClickListener(v -> {
            String eventName = organizerCreateEventName.getText().toString().trim();
            String attendanceLimit = organizerLimitAttendance.getText().toString().trim();
            String eventDetails = eventDetailsEditText.getText().toString();

            if (eventName.isEmpty()) { // Check if the event name is empty
                Toast.makeText(getActivity(), "Please enter a valid event name.", Toast.LENGTH_SHORT).show();
            }

            eventAdded = new Event(eventName);
            eventAdded.setEventDetails(eventDetails);

            if (!attendanceLimit.isEmpty()) {
                try {
                    // Convert attendance limit to an integer
                    int limit = Integer.parseInt(attendanceLimit);

                    if (limit != Float.parseFloat(attendanceLimit)) {
                        Toast.makeText(getActivity(), "Attendance limit must be an integer.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (limit < 0) {
                        // Show a toast message indicating that attendance limit must be a positive integer
                        Toast.makeText(getActivity(), "Attendance limit must be a positive integer.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    eventAdded.setEventAttendanceLimit(limit);
                } catch (NumberFormatException e) {
                    // Handle invalid input for attendance limit (non-numeric input)
                    Toast.makeText(getActivity(), "Please enter a valid number for attendance limit.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                // Set attendance limit to -1 meaning unlimited if input is empty
                eventAdded.setEventAttendanceLimit(-1);
            }

            if (getActivity() instanceof OnEventAddedListener) {
                ((OnEventAddedListener) getActivity()).onEventAdded(eventAdded);
            }

            dismiss();
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
