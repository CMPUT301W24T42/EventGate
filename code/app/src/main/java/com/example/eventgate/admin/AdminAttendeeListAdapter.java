package com.example.eventgate.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventgate.attendee.Attendee;
import com.example.eventgate.attendee.AttendeeDB;
import com.example.eventgate.R;

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
     * Constructs a new AdminAttendeeListAdapter.
     *
     * @param context The context.
     * @param attendees  The list of attendees to be displayed.
     */
    public AdminAttendeeListAdapter(Context context, ArrayList<Attendee> attendees) {
        super(context, 0, attendees);
        this.attendees = attendees;
        this.context = context;
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

        TextView attendeeName = convertView.findViewById(R.id.list_item_name);
        Button adminDeleteButton = convertView.findViewById(R.id.delete_button);

        attendeeName.setText(attendee.getName());

        // this removes attendees from the app and database once the admin clicks on the delete button
        adminDeleteButton.setOnClickListener(v -> {
//            attendees.remove(position);
//            attendeeDB.removeAttendee(attendee);
//            notifyDataSetChanged();
        });

        return convertView;
    }
}


