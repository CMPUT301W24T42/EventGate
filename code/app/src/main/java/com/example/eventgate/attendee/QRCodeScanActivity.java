// Activity that handles all QR-code scanning

package com.example.eventgate.attendee;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.eventgate.event.EventDB;
import com.example.eventgate.organizer.OrganizerAlert;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The activity for scanning QR-codes
 * It scans and manages the result of QR-codes
 */
public class QRCodeScanActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 1;
    private static final int RESULT_NOT_FOUND = 2;
    private static final int RESULT_REDUNDANT = 3;
    private GoogleApiClient googleApiClient;

    /**
     * Called when the activity is starting.
     * Checks and requests permissions, then initializes the QR-code scan
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
            PERMISSION_REQUEST_CAMERA);
        } else {
            initQRCodeScan();
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Handles the user's camera permissions
     *
     * @param requestCode Type of permission request
     * @param permissions Current permissions
     * @param grantResults Whether the user wishes to grant permission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initQRCodeScan();
            } else {
                Toast.makeText(this, "Camera permission is required",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void initQRCodeScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setOrientationLocked(true);
        integrator.setPrompt("Scan a QR Code");
        integrator.initiateScan();
    }

    /**
     * Based on the scan result, finish the activity accordingly
     *
     * @param requestCode Type of permission request
     * @param resultCode Type of result
     * @param data The resultant QR-code scan
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_LONG).show();
            } else {
                String qrResult = result.getContents().trim();

                if (qrResult.endsWith("_details")) {
                    // QR code is for event details
                    // Extract the event ID from qrResult (remove "_details" suffix)
                    String eventId = qrResult.substring(0, qrResult.length() - "_details".length());

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("events").document(eventId).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    // Retrieve the event name from Firestore
                                    String eventName = documentSnapshot.getString("name");

                                    // Retrieve the array of alerts if it exists
                                    ArrayList<Map> alerts = new ArrayList<>();
                                    if (documentSnapshot.contains("alerts") && documentSnapshot.get("alerts") != null) {
                                        alerts = (ArrayList<Map>) documentSnapshot.get("alerts");
                                    }

                                    Intent intent = new Intent(this, AttendeeAllEventViewerDetail.class);
                                    intent.putExtra("EventID", eventId);
                                    intent.putExtra("EventName", eventName);
                                    intent.putExtra("alerts", alerts);
                                    startActivity(intent);
                                }
                            });



                } else {
                    // QR code is for event check-in
                    FirebaseInstallations.getInstance().getId().addOnSuccessListener(id -> {
                        EventDB eventDb = new EventDB();
                        CompletableFuture<Integer> checkInResult = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            checkInResult = eventDb.checkInAttendee(id, qrResult, this);
                        }
                        checkInResult.thenAccept(r -> {
                            Intent intent = new Intent();
                            switch (r) {
                                case 0:
                                    setResult(RESULT_OK);
                                    break;
                                case 1:
                                    setResult(RESULT_NOT_FOUND, intent);
                                    break;
                                case 2:
                                    setResult(RESULT_REDUNDANT, intent);
                                    break;
                            }
                            finish();
                        });
                    });
                }
            }
        } else {
            super.onActivityResult(resultCode, resultCode, data);
        }
    }
}
