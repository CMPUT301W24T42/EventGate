/*
 * OrganizerEditQR.java
 *
 * This class is responsible for allowing organizers to edit QR codes associated with an event.
 * It provides functionality to generate and display QR codes for event check-in and description.
 * It also allows Organizers to share a QR Code to another app and reuse existing QR codes if
 * available.
 *
 * Citations:   https://developer.android.com/training/sharing/send#java
 *              https://www.youtube.com/watch?v=BWZv0iynWkE
 */

package com.example.eventgate.organizer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventgate.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides functionality for organizers to edit QR codes associated with an event.
 * Organizers can generate, display, and reuse QR codes for event check-in and description.
 */
public class OrganizerEditQR extends AppCompatActivity {
    private Bitmap eventQRBitmap;
    private Bitmap descriptionQRBitmap;
    private Boolean checkInQRGenerated = false;
    private Boolean descriptionQRGenerated = false;
    private String eventId;
    private String eventName;

    /**
     * Called when the activity is starting. Responsible for initializing the activity, views,
     * and retrieving existing QR code data from Firestore if available.
     *
     * @param savedInstanceState a Bundle containing the activity's previously saved state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_edit_qr);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView checkInQRCode = findViewById(R.id.organizerEditCheckInQRCode);
        ImageView descriptionQRCode = findViewById(R.id.organizerEditDescriptionQRCode);

        Button generateCheckInQRCodeButton = findViewById(R.id.generateCheckInQRButton);
        Button reuseQRButton = findViewById(R.id.ReuseQR);
        Button generateDescriptionQRButton = findViewById(R.id.generateDescriptionQRButton);
        Button shareCheckInQRCode = findViewById(R.id.OrganizerEditQRShareCheckInButton);
        Button shareDetailsQRCode = findViewById(R.id.OrganizerEditQRShareDetailsButton);
        Button backButton = findViewById(R.id.OrganizerEditQRBackButton);

        Intent previousIntent = getIntent();
        if (previousIntent != null) {
            eventId = previousIntent.getStringExtra("eventId");
            eventName = previousIntent.getStringExtra("eventName");
        }

        // Accessing Firestore and retrieving the QR code data if they have already been generated
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        assert eventId != null;
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the QR code data as a string from Firestore
                        String qrCodeDataString = documentSnapshot.getString("checkInQRCode");

                        // Parse the string representation back into an array of integers
                        if (qrCodeDataString != null && !qrCodeDataString.isEmpty()) {
                            String[] qrCodeArray = qrCodeDataString.substring(1, qrCodeDataString.length() - 1).split(", ");
                            int[] qrCodeIntArray = new int[qrCodeArray.length];
                            for (int i = 0; i < qrCodeArray.length; i++) {
                                qrCodeIntArray[i] = Integer.parseInt(qrCodeArray[i]);
                            }

                            // Convert the array of integers to a byte array
                            byte[] qrCodeByteArray = new byte[qrCodeIntArray.length];
                            for (int i = 0; i < qrCodeIntArray.length; i++) {
                                qrCodeByteArray[i] = (byte) qrCodeIntArray[i];
                            }

                            // Decode the byte array into a Bitmap
                            eventQRBitmap = BitmapFactory.decodeByteArray(qrCodeByteArray, 0, qrCodeByteArray.length);

                            // Set the Bitmap to the ImageView
                            checkInQRCode.setImageBitmap(eventQRBitmap);
                            checkInQRGenerated = true;
                        }

                        // Now the same with the other QR Code
                        String descriptionQRCodeDataString = documentSnapshot.getString("descriptionQRCode");

                        // Parse the string representation back into an array of integers
                        if (descriptionQRCodeDataString != null && !descriptionQRCodeDataString.isEmpty()) {
                            String[] descriptionQRCodeArray = descriptionQRCodeDataString.substring(1, descriptionQRCodeDataString.length() - 1).split(", ");
                            int[] descriptionQRCodeIntArray = new int[descriptionQRCodeArray.length];
                            for (int i = 0; i < descriptionQRCodeArray.length; i++) {
                                descriptionQRCodeIntArray[i] = Integer.parseInt(descriptionQRCodeArray[i]);
                            }

                            // Convert the array of integers to a byte array
                            byte[] descriptionQRCodeByteArray = new byte[descriptionQRCodeIntArray.length];
                            for (int i = 0; i < descriptionQRCodeIntArray.length; i++) {
                                descriptionQRCodeByteArray[i] = (byte) descriptionQRCodeIntArray[i];
                            }

                            // Decode the byte array into a Bitmap
                            descriptionQRBitmap = BitmapFactory.decodeByteArray(descriptionQRCodeByteArray, 0, descriptionQRCodeByteArray.length);

                            // Set the Bitmap to the ImageView
                            descriptionQRCode.setImageBitmap(descriptionQRBitmap);
                            descriptionQRGenerated = true;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                });

        generateCheckInQRCodeButton.setOnClickListener(v -> {
            if (!checkInQRGenerated) {
                // Create Check in QR Code
                MultiFormatWriter writer = new MultiFormatWriter();

                try {
                    BitMatrix matrix = writer.encode(eventId, BarcodeFormat.QR_CODE, 400, 400);
                    BarcodeEncoder encoder = new BarcodeEncoder();
                    eventQRBitmap = encoder.createBitmap(matrix);

                } catch (WriterException e) {
                    e.printStackTrace();
                }

                // Convert bitmap to byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                eventQRBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] byteArray = baos.toByteArray();

                // Convert byte array to list of integers
                List<Integer> byteArrayAsList = new ArrayList<>();
                for (byte b : byteArray) {
                    byteArrayAsList.add((int) b);
                }

                // Store the QR code string in Firestore
                FirebaseFirestore.getInstance().collection("events").document(eventId)
                        .update("checkInQRCode", byteArrayAsList.toString())
                        .addOnSuccessListener(aVoid -> {
                            // Update successful
                            checkInQRGenerated = true;
                        })
                        .addOnFailureListener(e -> {
                        });

                // Update ImageView
                checkInQRCode.setImageBitmap(eventQRBitmap);
            }
        });

        reuseQRButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerEditQR.this, OrganizerReuseQRActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });

        generateDescriptionQRButton.setOnClickListener(v -> {
            if (!descriptionQRGenerated) {
                String uniqueIdentifier = eventId + "_details";

                // Create Description QR Code
                MultiFormatWriter writer = new MultiFormatWriter();

                try {
                    BitMatrix matrix = writer.encode(uniqueIdentifier, BarcodeFormat.QR_CODE, 400, 400);
                    BarcodeEncoder encoder = new BarcodeEncoder();
                    descriptionQRBitmap = encoder.createBitmap(matrix);

                } catch (WriterException e) {
                    e.printStackTrace();
                }

                // Convert bitmap to byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                descriptionQRBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] byteArray = baos.toByteArray();

                // Convert byte array to list of integers
                List<Integer> byteArrayAsList = new ArrayList<>();
                for (byte b : byteArray) {
                    byteArrayAsList.add((int) b);
                }

                // Store the QR code string in Firestore
                FirebaseFirestore.getInstance().collection("events").document(eventId)
                        .update("descriptionQRCode", byteArrayAsList.toString())
                        .addOnSuccessListener(aVoid -> {
                            // Update successful
                            descriptionQRGenerated = true;
                        })
                        .addOnFailureListener(e -> {
                        });

                // Update ImageView
                descriptionQRCode.setImageBitmap(descriptionQRBitmap);
            }
        });

        shareCheckInQRCode.setOnClickListener(v -> {
            if (eventQRBitmap != null) {
                shareImage(eventQRBitmap);
            }
        });

        shareDetailsQRCode.setOnClickListener(v -> {
            if (descriptionQRBitmap != null) {
                shareImage(descriptionQRBitmap);
            }
        });

        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Shares an image using an Intent.ACTION_SEND. This method allows the user to share the provided
     * bitmap image via various sharing platforms installed on the device.
     *
     * @param bitmap The bitmap image to be shared.
     */
    private void shareImage(Bitmap bitmap) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        Uri bmpUri;
        String textToShare = "Share QR Code";

        bmpUri = saveImage(bitmap, getApplicationContext());

        share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share.putExtra(Intent.EXTRA_STREAM, bmpUri);
        share.putExtra(Intent.EXTRA_SUBJECT, "EventGate");
        share.putExtra(Intent.EXTRA_TEXT, textToShare);

        startActivity(Intent.createChooser(share, "Share Content"));
    }

    /**
     * Saves a bitmap image to the cache directory and returns the URI of the saved image.
     *
     * @param image   The bitmap image to be saved.
     * @param context The context of the application.
     * @return The URI of the saved image.
     */
    private static Uri saveImage(Bitmap image, Context context) {
        File imagesFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, "shared_images.jpg");

            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(Objects.requireNonNull(context.getApplicationContext()), "com.example.eventgate" + ".provider", file);
        } catch (IOException e){
            Log.d("TAG", "Exception" + e.getMessage());
        }

        return uri;
    }
}
