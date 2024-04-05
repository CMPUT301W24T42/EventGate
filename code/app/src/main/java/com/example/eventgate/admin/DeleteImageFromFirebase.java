package com.example.eventgate.admin;

import android.util.Log;

import com.example.eventgate.MainActivity;
import com.example.eventgate.attendee.Attendee;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public final class DeleteImageFromFirebase {
    private DeleteImageFromFirebase() {

    }

    /**
     * this deletes the event poster from firestore
     * @param imageUrl the url of the image to be deleted
     */
    static void deletePosterFromFirestore(String imageUrl, CollectionReference postersRef) {
        postersRef.whereEqualTo("url", imageUrl).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                snapshot.getReference().delete()
                        .addOnSuccessListener(unused -> Log.d("Firebase", "Event poster successfully deleted from Firestore"))
                        .addOnFailureListener(e -> Log.d("Firebase", "Error deleting event poster from Firestore", e));
            }
        });
    }

    /**
     * this deletes the event poster from firebase cloud storage
     * @param imageUrl the url of the image to be deleted
     */
    static void deletePosterFromCloudStorage(String imageUrl) {
        // get the name of the image from the url of the image
        String imageName = imageUrl.substring(imageUrl.indexOf("images%2F") + 9, imageUrl.indexOf(".jpg?"));

        // get reference to the image in firebase cloud storage and delete it
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference fileReference = storageRef.child("images/" + imageName + ".jpg");
        fileReference.delete()
                .addOnSuccessListener(unused -> Log.d("Firebase", "Event poster successfully deleted from Storage"))
                .addOnFailureListener(e -> Log.d("Firebase", "Error deleting event poster from Storage", e));

    }

    static void deleteProfilePicFromFirestore(String attendeeId) {
        CollectionReference attendeesRef = MainActivity.db.getAttendeesRef();
        attendeesRef.document(attendeeId).update("profilePicturePath", "");
    }

    static void deleteProfilePicFromCloudStorage(String deviceId) {
        // get reference to the image in firebase cloud storage and delete it
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference fileReference = storageRef.child("images/" + deviceId + ".jpg");
        fileReference.delete()
                .addOnSuccessListener(unused -> Log.d("Firebase", "Profile picture successfully deleted from Storage"))
                .addOnFailureListener(e -> Log.d("Firebase", "Error deleting profile picture from Storage", e));
    }
}
