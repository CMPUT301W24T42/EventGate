package com.example.eventgate.admin;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventgate.MainActivity;
import com.example.eventgate.attendee.Attendee;
import com.example.eventgate.attendee.AttendeeDB;
import com.example.eventgate.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;

/**
 * This is an adapter for displaying a list of attendees that includes a delete button that the admin
 *     can use
 */
public class AdminAttendeeListAdapter extends ArrayAdapter<Attendee> {
    /**
     * this is the list of attendees that will be displayed by the adapter
     */
    private ArrayList<Attendee> attendees;
    /**
     * this holds the context
     */
    private Context context;
    /**
     * this holds an event's id
     */
    private String eventId;
    /**
     * a reference to the attendees collection in the database
     */
    private final CollectionReference collection = MainActivity.db.getAttendeesRef();
    /**
     * a tag for logging
     */
    private final String TAG = "Firestore";

    /**
     * Constructs a new AdminAttendeeListAdapter.
     *
     * @param context The context.
     * @param attendees  The list of attendees to be displayed.
     */
    public AdminAttendeeListAdapter(Context context, ArrayList<Attendee> attendees, String eventId) {
        super(context, 0, attendees);
        this.attendees = attendees;
        this.context = context;
        this.eventId = eventId;
    }

    /**
     * Returns a view that displays the event at the specified position in the list.
     *
     * @param position    The position of the event in the list.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent view that this view will eventually be attached to.
     * @return A View corresponding to the event at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.admin_list_item, parent, false);
        }

        Attendee attendee = attendees.get(position);
        AttendeeDB attendeeDB = new AttendeeDB();

        // get references to views
        TextView attendeeName = convertView.findViewById(R.id.list_item_name);
        Button adminDeleteButton = convertView.findViewById(R.id.delete_button);

        attendeeName.setText(attendee.getName());

        // set up click listener for delete button
        setDeleteClickListener(adminDeleteButton, position, attendeeDB, attendee);

        return convertView;
    }

    /**
     * this sets up the clicklistener for the delete button on the attendees list
     * @param adminDeleteButton the button whose click listener is being set
     * @param position the position of the attendee in the attendee list
     * @param attendeeDB an instance of AttendeeDB
     * @param attendee the attendee that we want to delete
     */
    private void setDeleteClickListener(Button adminDeleteButton, int position, AttendeeDB attendeeDB, Attendee attendee) {
        // this removes attendees from the app and database once the admin clicks on the delete button
        adminDeleteButton.setOnClickListener(v -> {
            // get a title and message for the confirm delete dialog
            Resources resources = context.getResources();
            String title = String.format("%s %s?", resources.getString(R.string.delete_title), attendee.getName());
            String message = String.format("%s %s", attendee.getName(), resources.getString(R.string.delete_message));
            // create a ConfirmDeleteDialog to ask user for confirmation when deleting attendees
            ConfirmDeleteDialog confirmDeleteDialog = ConfirmDeleteDialog.newInstance(title, message);
            confirmDeleteDialog.setOnDeleteClickListener(() -> {
                attendees.remove(position);
                // if delete button is clicked in AdminActivity then remove attendee as a whole, if clicked
                //      from AdminEventViewerActivity then only remove attendee from specified event
                if (context instanceof AdminActivity) {
                    // removes attendee from all events if the delete button is being clicked from AdminActivity
                    removeAttendeeFromAllEvents(attendee);
                    removeAttendee(attendee);  // remove attendee profile from database
                } else {
                    // removes attendee from specified event if button is being clicked from AdminEventViewerActivity
                    removeAttendeeFromEvent(attendee, eventId);
                }
                notifyDataSetChanged();
            });
            confirmDeleteDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "CONFIRM DELETE ATTENDEE");
        });
    }

    private void removeAttendee(Attendee attendee) {
        String attendeeId = attendee.getAttendeeId();
        // delete the attendees info/profile from firestore
        collection.document(attendeeId).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Attendee successfully deleted!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error deleting attendee", e));

    }

    /**
     * this removes an attendee from the specified event (used by admin)
     * @param attendee the attendee that will be removed
     * @param eventId the event that the attendee will be removed from
     */
    private void removeAttendeeFromEvent(Attendee attendee, String eventId) {
        String attendeeId = attendee.getAttendeeId();
        // get references to the attendee's document and the event's document in the database
        DocumentReference attendeeRef = collection.document(attendeeId);
        DocumentReference eventRef = MainActivity.db.getEventsRef().document(eventId);

        eventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // remove attendee from event and event from attendee
                    eventRef.update("attendees", FieldValue.arrayRemove(attendeeId));
                    attendeeRef.update("events", FieldValue.arrayRemove(eventId));
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    /**
     * removes an attendee from all the events they are attending in the case that the attendee/user
     *      is removed by an admin
     * @param attendee the attendee to be removed
     */
    private void removeAttendeeFromAllEvents(Attendee attendee) {
        String attendeeId = attendee.getAttendeeId();
        // get references to the attendee's document and the events collection in the database
        DocumentReference attendeeRef = collection.document(attendeeId);
        CollectionReference eventsRef = MainActivity.db.getEventsRef();

        attendeeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // get the list of events that the attendee is attending
                    ArrayList<String> eventsList = (ArrayList<String>) document.get("events");
                    // go through events and remove the attendee from the event's list of attendees
                    //      and remove the event from the attendee's list of events
                    for (String eventId : eventsList) {
                        eventsRef.document(eventId).update("attendees", FieldValue.arrayRemove(attendeeId));
                        attendeeRef.update("events", FieldValue.arrayRemove(eventId));
                    }
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }
}


