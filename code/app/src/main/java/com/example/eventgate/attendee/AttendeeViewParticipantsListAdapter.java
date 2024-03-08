package com.example.eventgate.attendee;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.eventgate.event.Event;
import com.example.eventgate.R;

import java.util.ArrayList;

/**
 * Adapter for Activity for attendee to view all participants of events they themselves are in
 */

public class AttendeeViewParticipantsListAdapter extends ArrayAdapter<String> {
    public AttendeeViewParticipantsListAdapter(Context context, ArrayList<String> attendees) {
        super(context, 0, attendees);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String attendee = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_attendee_view_event_participants_listview, parent, false);
        }

        TextView attendeeName = convertView.findViewById(R.id.tvAttendeeName);

        attendeeName.setText(attendee);

        return convertView;
    }
}