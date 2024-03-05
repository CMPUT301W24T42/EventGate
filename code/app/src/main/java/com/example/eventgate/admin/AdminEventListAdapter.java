package com.example.eventgate.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventgate.Event.Event;
import com.example.eventgate.R;

import java.util.ArrayList;

public class AdminEventListAdapter extends ArrayAdapter<Event> {
    private ArrayList<Event> events;
    private Context context;
    public AdminEventListAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        this.events = events;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.admin_events_list, parent, false);
        }

        Event event = events.get(position);

        TextView eventName = convertView.findViewById(R.id.event_name);

        eventName.setText(event.getEventName());

        return convertView;
    }
}
