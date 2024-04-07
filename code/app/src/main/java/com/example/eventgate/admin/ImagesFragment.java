package com.example.eventgate.admin;

import static com.example.eventgate.admin.DeleteImageFromFirebase.deletePosterFromCloudStorage;
import static com.example.eventgate.admin.DeleteImageFromFirebase.deletePosterFromFirestore;
import static com.example.eventgate.admin.DeleteImageFromFirebase.deleteProfilePicFromCloudStorage;
import static com.example.eventgate.admin.DeleteImageFromFirebase.deleteProfilePicFromFirestore;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.example.eventgate.MainActivity;
import com.example.eventgate.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
                    ImageData imageData;
                    // id associated with the image will either be eventId (event posters) or attendeeId (profile picture)
                    if (doc.getString("eventId") != null) {
                        imageData = new ImageData("poster", doc.getString("eventId"));
                    } else {
                        imageData =  new ImageData("pfp", doc.getString("attendeeId"));
                    }
                    map.put(imageUrl, imageData);
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
        String imageUrl = imageList.get(position);
        ImageData data = (ImageData) map.get(imageUrl);
        String imageType = data.getImageType();
        String id = data.getAssociatedId();

        if (imageType.equals("poster")) {
            CollectionReference postersRef = MainActivity.db.getEventsRef().document(id).collection("posters");
            deletePosterFromFirestore(imageUrl, postersRef);
            deletePosterFromCloudStorage(imageUrl);
        } else {  // image type is pfp (profile picture)
            deleteProfilePicFromFirestore(id, imageUrl);
            CollectionReference attendeesRef = MainActivity.db.getAttendeesRef();
            attendeesRef.document(id).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String deviceId = task.getResult().getString("deviceId");
                    deleteProfilePicFromCloudStorage(deviceId);
                }
            });
        }
        // remove image url from imageList
        imageList.remove(position);
        // remove the imageUrl along with its data (ImageData) from the map
        map.remove(imageUrl);
        gridViewAdapter.notifyDataSetChanged();
    }
}