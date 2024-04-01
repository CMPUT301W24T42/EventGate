package com.example.eventgate.admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.eventgate.MainActivity;
import com.example.eventgate.R;
import com.example.eventgate.event.Event;
import com.example.eventgate.organizer.CreateAlertFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

// citations
// https://www.youtube.com/watch?v=aRgSrJO40z8&t=690s Elements of the following layout design,
//      (fragment_images) is from Foxandroid, Youtube, "How to Implement GridView in Android Studio
//      || GridView || Android Studio Tutorial", 2021-04-27
/**
 * a fragment used to display images in a gridview
 */
public class ImagesFragment extends Fragment {
    /**
     * the list of images to be displayed
     */
    ArrayList<String> imageList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_images, container, false);

        // find layout references and set adapters
        GridView gridView = view.findViewById(R.id.image_grid_view);
        imageList = new ArrayList<>();
        AdminGridViewAdapter gridViewAdapter = new AdminGridViewAdapter(getContext(), imageList);
        gridView.setAdapter(gridViewAdapter);

        // snapshot listener to add/update event posters from the database
        CollectionReference eventsRef = MainActivity.db.getEventsRef();
        eventsRef.addSnapshotListener((queryDocumentSnapshots, error) -> {
            for(QueryDocumentSnapshot doc: queryDocumentSnapshots)
            {
                // get posters collection for each event document
                CollectionReference postersRef = eventsRef.document(doc.getId()).collection("posters");
                postersRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // add urls from posters collection
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String imageUrl = document.getString("url");
                            imageList.add(imageUrl);
                        }
                        gridViewAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        // set on item click listener
        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            ImagePopUpDialog fragment = new ImagePopUpDialog();
            // create a bundle so we can access the eventId in the dialog fragment
            Bundle args = new Bundle();
            args.putString("imageUrl", imageList.get(position));
            fragment.setArguments(args);
            fragment.show(getActivity().getSupportFragmentManager(), "IMAGE POPUP");
        });

        return view;
    }
}