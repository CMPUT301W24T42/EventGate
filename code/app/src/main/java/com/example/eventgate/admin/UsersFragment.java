package com.example.eventgate.admin;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.eventgate.MainActivity;
import com.example.eventgate.R;
import com.example.eventgate.attendee.Attendee;
import com.example.eventgate.attendee.AttendeeDB;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * this is a fragment to show a list of users/attendees
 */
public class UsersFragment extends Fragment {
    /**
     * this is an array list that holds attendees
     */
    ArrayList<Attendee> attendeeDataList;
    /**
     * this is an adapter for displaying a list of attendees
     */
    ArrayAdapter<Attendee> attendeeAdapter;
    /**
     * This is the listview that displays the attendees in the attendeeDataList
     */
    ListView attendeeList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_attendees, container, false);
        // create the attendee list and set adapter
        createAttendeeList(fragmentView);
        // get reference to attendees collection
        CollectionReference attendeesRef = MainActivity.db.getAttendeesRef();
        // snapshot listener that adds/updates all the attendees/users from the database
        AttendeeDB attendeeDB = new AttendeeDB();
        attendeesRef.addSnapshotListener((queryDocumentSnapshots, error) -> {
            attendeeDataList.clear();
            for(QueryDocumentSnapshot doc: queryDocumentSnapshots)
            {
                Attendee attendee = new Attendee((String) doc.getData().get("name"),
                        (String) doc.getData().get("deviceId"), doc.getId());
                attendeeDB.getAttendeeInfo((String) doc.getData().get("deviceId"), attendee);
                attendeeDataList.add(attendee);
                attendeeAdapter.notifyDataSetChanged();
            }
        });

        // starts a new activity to view event info including attendees of event
        attendeeList.setOnItemClickListener((parent, view, position, id) -> {
            // pop dialog to show all user info
            UserInfoDialog fragment = new UserInfoDialog();
            // create a bundle so we can access
            Bundle args = new Bundle();
            args.putSerializable("attendee", attendeeDataList.get(position));
            fragment.setArguments(args);
            fragment.show(getActivity().getSupportFragmentManager(), "IMAGE POPUP");  // show dialog
        });

        return fragmentView;
    }

    /**
     * creates the attendee list and sets the adapter
     */
    private void createAttendeeList(View view) {
        attendeeDataList = new ArrayList<>();

        attendeeList = view.findViewById(R.id.user_list);

        attendeeAdapter = new AdminAttendeeListAdapter(getActivity(), attendeeDataList, "");
        attendeeList.setAdapter(attendeeAdapter);
    }
}