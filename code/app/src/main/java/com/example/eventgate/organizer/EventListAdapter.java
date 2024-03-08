package com.example.eventgate.organizer;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.eventgate.event.Event;

import java.util.ArrayList;

/**
 * Adapter for displaying a list of events.
 */
public class EventListAdapter extends ArrayAdapter<Event> {
    private final Context context;
    private final ArrayList<Event> events;

    /**
     * Constructs a new EventListAdapter.
     *
     * @param context The context.
     * @param events  The list of events to be displayed.
     */
    public EventListAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        this.context = context;
        this.events = events;
    }

    /**
     * Returns the number of events in the list.
     *
     * @return The number of events.
     */
    @Override
    public int getCount() {
        return events.size();
    }

    /**
     * Returns the event at the specified position in the list.
     *
     * @param position The position of the event in the list.
     * @return The event at the specified position.
     */
    @Override
    public Event getItem(int position) {
        return events.get(position);
    }

    /**
     * Returns the row ID of the specified event.
     *
     * @param position The position of the event in the list.
     * @return The row ID of the specified event.
     */
    @Override
    public long getItemId(int position) {
        return position;
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
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        String eventName = events.get(position).getEventName();

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(eventName);

        return convertView;
    }
}