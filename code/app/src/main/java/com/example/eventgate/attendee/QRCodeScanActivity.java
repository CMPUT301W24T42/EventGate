package com.example.eventgate.attendee;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class QRCodeScanActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 1;
    private static final int RESULT_NOT_FOUND = 2;
    private static final int RESULT_REDUNDANT = 3;

    private FirebaseFirestore db;
    private CollectionReference events;
    private ArrayList<String> attendees;
    private String deviceId;
    private String attendeeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
            PERMISSION_REQUEST_CAMERA);
        } else {
            initQRCodeScan();
        }
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_LONG).show();
            } else {
                String qrResult = result.getContents().trim();
                db = FirebaseFirestore.getInstance();
                db.collection("events").document(qrResult).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        attendees = (ArrayList<String>) documentSnapshot.get("attendees");
                        FirebaseInstallations.getInstance().getId().addOnSuccessListener(id -> {
                            deviceId = id;
                            db.collection("attendees").whereEqualTo("deviceId", deviceId).get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    attendeeId = queryDocumentSnapshots.getDocuments().get(0).getId();
                                    for (String attendee : attendees) {
                                        if (Objects.equals(attendee, attendeeId)) {
                                            Intent intent = new Intent();
                                            setResult(RESULT_REDUNDANT, intent);
                                            finish();
                                        }
                                    }
                                    Map<String, Object> updates = new HashMap<>();
                                    attendees.add(attendees.size() - 1, attendeeId);
                                    updates.put("attendees", attendees);
                                    db.collection("events").document(qrResult).update(updates);
                            });
                        });
                    } else {
                        Intent intent = new Intent();
                        setResult(RESULT_NOT_FOUND, intent);
                        finish();
                    }
                });
            }
        } else {
            super.onActivityResult(resultCode, resultCode, data);
        }
    }

}
