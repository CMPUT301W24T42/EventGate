package com.example.eventgate.admin;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
            convertView = LayoutInflater.from(context).inflate(R.layout.admin_list_item, parent, false);
        }

        Event event = events.get(position);
        EventDB eventDB = new EventDB();

        TextView eventName = convertView.findViewById(R.id.list_item_name);
        Button adminDelEventButton = convertView.findViewById(R.id.delete_button);

        eventName.setText(event.getEventName());

        // asks admin to confirm deletion and them deletes event from list and from database
        adminDelEventButton.setOnClickListener(v -> {
            // get a title and message for the confirm delete dialog
            Resources resources = context.getResources();
            String title = String.format("%s %s?", resources.getString(R.string.delete_title), event.getEventName());
            String message = String.format("%s %s", event.getEventName(), resources.getString(R.string.delete_message));
            // create dialog to confirm deletion
            ConfirmDeleteDialog confirmDeleteDialog = ConfirmDeleteDialog.newInstance(title, message);
            confirmDeleteDialog.setOnDeleteClickListener(() -> {
                // delete event from event list and from firebase
                events.remove(position);
                eventDB.removeEvent(event);
                notifyDataSetChanged();
            });
            confirmDeleteDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "CONFIRM DELETE EVENT");
        });

        return convertView;
    }
}
