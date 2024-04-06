package com.example.eventgate.admin;

import android.util.Log;


import com.example.eventgate.MainActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * this handles the deletion of images from Firestore and Firebase Cloud Storage
 */
public final class DeleteImageFromFirebase {
    /**
     * private constructor to prevent instantiation of object
     */
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

    /**
     * this deletes the profile picture from firestore
     * @param attendeeId the id of the attendee whose profile picture will be deleted
     * @param imageUrl the url of the image to be deleted
     */
    static void deleteProfilePicFromFirestore(String attendeeId, String imageUrl) {
        CollectionReference attendeesRef = MainActivity.db.getAttendeesRef();
        // remove profile picture from attendee's document in firestore
        attendeesRef.document(attendeeId).update("profilePicturePath", "");
        CollectionReference imagesRef = MainActivity.db.getImagesRef();
        // remove profile picture from images collection in firestore
        imagesRef.whereEqualTo("url", imageUrl).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc: task.getResult()) {
                    imagesRef.document(doc.getId()).delete().addOnSuccessListener(unused -> {
                        Log.d("Firestore", "Successfully removed profile picture from images collection");
                    });
                }
            }
        });
    }

    /**
     * this deletes the profile picture from cloud storage
     * @param deviceId the deviceId of the user whose profile picture will be deleted
     */
    static void deleteProfilePicFromCloudStorage(String deviceId) {
        // get reference to the image in firebase cloud storage and delete it
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference fileReference = storageRef.child("images/" + deviceId + ".jpg");
        fileReference.delete()
                .addOnSuccessListener(unused -> Log.d("Firebase", "Profile picture successfully deleted from Storage"))
                .addOnFailureListener(e -> Log.d("Firebase", "Error deleting profile picture from Storage", e));
    }
}
