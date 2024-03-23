package com.example.eventgate.organizer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventgate.R;

import java.util.ArrayList;

/**
 * An adapter for displaying the events an attendee is checked into
 */
public class AttendeeListAdapter extends ArrayAdapter<String> {
    private ArrayList<String> attendees;
    private Context context;

    /**
     * Constructs a new AttendeeEventListAdapter.
     *
     * @param context The context.
     * @param attendees  The list of attendees to display
     */
    public AttendeeListAdapter(Context context, ArrayList<String> attendees) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.attendee_events_list, parent,
                    false);
        }

        String attendee = attendees.get(position);

        TextView eventName = convertView.findViewById(R.id.list_item_name);

        eventName.setText(attendee);

        return convertView;
    }
}
