/*
 * OrganizerEditQR.java
 *
 * This class is responsible for allowing organizers to edit QR codes associated with an event.
 * It provides functionality to generate and display QR codes for event check-in and description.
 * Organizers can also reuse existing QR codes if available and share existing qr codes.
 *
 * Citation:
 */

package com.example.eventgate.organizer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.ArrayList;
import java.util.List;

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
        Button shareQRCode = findViewById(R.id.OrganizerEditQRShareButton);
        Button backButton = findViewById(R.id.OrganizerEditQRBackButton);

        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

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
                // Create Description QR Code
                MultiFormatWriter writer = new MultiFormatWriter();

                try {
                    BitMatrix matrix = writer.encode(eventName, BarcodeFormat.QR_CODE, 400, 400);
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

        backButton.setOnClickListener(v -> finish());
    }
}
