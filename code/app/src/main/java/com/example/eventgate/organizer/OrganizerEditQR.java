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

public class OrganizerEditQR extends AppCompatActivity {

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

        Button backButton = findViewById(R.id.OrganizerEditQRBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Accessing Firestore and retrieving the QR code data
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String eventId = getIntent().getStringExtra("eventId");
        assert eventId != null;
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the QR code data as a string from Firestore
                        String qrCodeDataString = documentSnapshot.getString("checkInQRCode");

                        // Parse the string representation back into an array of integers
                        assert qrCodeDataString != null;
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
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failures
                });
    }
}