package com.example.eventgate.attendee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventgate.R;
import com.example.eventgate.organizer.OrganizerAlert;

import java.util.ArrayList;

/**
 * An adapter for displaying the alerts of an event
 */
public class AlertListAdapter extends ArrayAdapter<OrganizerAlert> {
    private ArrayList<OrganizerAlert> alerts;
    private Context context;

    /**
     * Constructs a new AlertListAdapter.
     *
     * @param context The context.
     * @param alerts  The list of alerts to be displayed.
     */
    public AlertListAdapter(Context context, ArrayList<OrganizerAlert> alerts) {
        super(context, 0, alerts);
        this.alerts = alerts;
        this.context = context;
    }

    /**
     * Returns a view that displays the alert at the specified position in the list.
     *
     * @param position    The position of the alert in the list.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent view that this view will eventually be attached to.
     * @return A View corresponding to the event at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.attendee_alerts_list, parent,
                    false);
        }

        OrganizerAlert alert = alerts.get(position);

        TextView alertTitle = convertView.findViewById(R.id.alert_title);

        alertTitle.setText(alert.getTitle());

        return convertView;
    }
}
