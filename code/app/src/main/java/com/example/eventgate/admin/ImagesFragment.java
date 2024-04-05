package com.example.eventgate.admin;

import static com.example.eventgate.admin.DeleteImageFromFirebase.deletePosterFromCloudStorage;
import static com.example.eventgate.admin.DeleteImageFromFirebase.deletePosterFromFirestore;

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
import java.util.HashMap;

// citations
// https://www.youtube.com/watch?v=aRgSrJO40z8&t=690s Elements of the following layout design,
//      (fragment_images) is from Foxandroid, Youtube, "How to Implement GridView in Android Studio
//      || GridView || Android Studio Tutorial", 2021-04-27
/**
 * a fragment used to display images in a gridview
 */
public class ImagesFragment extends Fragment implements ImagePopUpDialog.OnImageDeleteListener {
    /**
     * the list of images to be displayed
     */
    ArrayList<String> imageList;
    /**
     * adapter for displaying images in gridview
     */
    AdminGridViewAdapter gridViewAdapter;
    /**
     * holds image urls and their associated eventId or attendeeId
     */
    HashMap<Object, Object> map;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_images, container, false);

        // find layout references and set adapters
        GridView gridView = view.findViewById(R.id.image_grid_view);
        imageList = new ArrayList<>();
        gridViewAdapter = new AdminGridViewAdapter(getContext(), imageList);
        gridView.setAdapter(gridViewAdapter);
        map = new HashMap<>();

        // snapshot listener to add/update images from the database
        CollectionReference imagesRef = MainActivity.db.getImagesRef();
        imagesRef.addSnapshotListener((value, error) -> {
            if (value != null) {
                imageList.clear();
                for (QueryDocumentSnapshot doc: value) {
                    String imageUrl = doc.getString("url");
                    String id;
                    // id associated with the image will either be eventId (event posters) or attendeeId (profile picture)
                    if (doc.getString("eventId") != null) {
                        id = doc.getString("eventId");
                    } else {
                        id = doc.getString("attendeeId");
                    }
                    if (!map.containsKey(imageUrl)) {
                        map.put(imageUrl, id);
                    }
                    imageList.add(imageUrl);
                }
                gridViewAdapter.notifyDataSetChanged();
            }

        });

        // set on item click listener
        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            ImagePopUpDialog fragment = new ImagePopUpDialog();
            // create a bundle so we can access the eventId and position in the dialog fragment
            Bundle args = new Bundle();
            args.putString("imageUrl", imageList.get(position));
            args.putInt("position", position);
            fragment.setArguments(args);

            fragment.setOnImageDeleteListener(this); // Set listener
            fragment.show(getActivity().getSupportFragmentManager(), "IMAGE POPUP");  // show dialog
        });


        return view;
    }

    /**
     * delete the image from the list of images, from firestore, and from cloud storage
     * @param position the position of the image in the gridview
     */
    @Override
    public void onImageDelete(int position) {
        // get the image url and the event id associated with that image
        String imageUrl = imageList.get(position);
        // get a reference to the posters subcollection contained that poster
//        CollectionReference postersRef = MainActivity.db.getEventsRef().document(eventId).collection("posters");
        // delete the poster from firestore and cloud storage
//        deletePosterFromFirestore(imageUrl, postersRef);
        deletePosterFromCloudStorage(imageUrl);
        // remove image url and event id from their respective lists
        imageList.remove(position);

        gridViewAdapter.notifyDataSetChanged();
    }
}