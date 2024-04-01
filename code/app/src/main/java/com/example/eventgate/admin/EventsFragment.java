package com.example.eventgate.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.eventgate.MainActivity;
import com.example.eventgate.R;
import com.example.eventgate.event.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class EventsFragment extends Fragment {
    /**
     * This is an array list that holds events
     */
    ArrayList<Event> eventDataList;
    /**
     * This is the listview that displays the events in the eventDataList
     */
    ListView eventList;
    /**
     * This is an adapter for displaying a list of events
     */
    ArrayAdapter<Event> eventAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_events, container, false);
        // create the event list and set adapter
        createEventList(fragmentView);
        // get reference to events collection
        CollectionReference eventsRef = MainActivity.db.getEventsRef();
        // snapshot listener that adds/updates all the events from the database
        eventsRef.addSnapshotListener((queryDocumentSnapshots, error) -> {
            eventDataList.clear();
            for(QueryDocumentSnapshot doc: queryDocumentSnapshots)
            {
                Event event = new Event((String) doc.getData().get("name"));
                event.setEventId(doc.getId());
                eventDataList.add(event);
                eventAdapter.notifyDataSetChanged();
            }
        });

        // starts a new activity to view event info including attendees of event
        eventList.setOnItemClickListener((parent, view, position, id) -> {
            Event event = eventDataList.get(position);
            Intent intent = new Intent(getActivity(), AdminEventViewerActivity.class);
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("name", event.getEventName());
            startActivity(intent);
        });

        return fragmentView;
    }

    /**
     * creates the event list and sets the adapter
     */
    private void createEventList(View view) {
        eventDataList = new ArrayList<>();

        eventList = view.findViewById(R.id.event_list);

        eventAdapter = new AdminEventListAdapter(getActivity(), eventDataList);
        eventList.setAdapter(eventAdapter);
    }
}