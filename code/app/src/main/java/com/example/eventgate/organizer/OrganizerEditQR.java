package com.example.eventgate.organizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
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

public class OrganizerEditQR extends AppCompatActivity {
    private Bitmap eventQRBitmap;
    private Bitmap descriptionQRBitmap;
    private Boolean checkInQRGenerated = false;
    private Boolean descriptionQRGenerated = false;
    private String eventId;
    private String eventName;

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

        // Accessing Firestore and retrieving the QR code data
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
                            Bitmap eventQRBitmap = BitmapFactory.decodeByteArray(qrCodeByteArray, 0, qrCodeByteArray.length);

                            // Set the Bitmap to the ImageView
                            checkInQRCode.setImageBitmap(eventQRBitmap);
                            checkInQRGenerated = true;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failures
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

                checkInQRCode.setImageBitmap(eventQRBitmap);
                checkInQRGenerated = true;
            }
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

                descriptionQRCode.setImageBitmap(descriptionQRBitmap);
                descriptionQRGenerated = true;
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}