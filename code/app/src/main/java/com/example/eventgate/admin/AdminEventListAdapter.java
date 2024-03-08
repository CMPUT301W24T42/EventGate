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

import com.example.eventgate.event.Event;
import com.example.eventgate.R;
import com.example.eventgate.event.EventDB;

import java.util.ArrayList;

/**
 * This is an adapter for displaying a list of events that includes a delete button that the admin
 *     can use
 */
public class AdminEventListAdapter extends ArrayAdapter<Event> {
    /**
     * this is the list of events that will be displayed by the adapter
     */
    private ArrayList<Event> events;
    /**
     * this holds the context
     */
    private Context context;

    /**
     * Constructs a new AdminEventListAdapter.
     *
     * @param context The context.
     * @param events  The list of events to be displayed.
     */
    public AdminEventListAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        this.events = events;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.admin_events_list, parent, false);
        }

        Event event = events.get(position);
        EventDB eventDB = new EventDB();

        TextView eventName = convertView.findViewById(R.id.event_name);
        Button adminDelEventButton = convertView.findViewById(R.id.del_event_button);

        eventName.setText(event.getEventName());

        // this removes events from the app and database once the admin clicks on the delete button
        adminDelEventButton.setOnClickListener(v -> {
            events.remove(position);
            eventDB.removeEvent(event);
            notifyDataSetChanged();
        });

        return convertView;
    }
}
