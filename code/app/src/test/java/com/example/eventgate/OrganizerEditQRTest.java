package com.example.eventgate;

import static org.junit.Assert.assertNotNull;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.junit.Test;

/**
 * This class contains unit tests for generating QR codes related to event organization.
 */
public class OrganizerEditQRTest {

    /**
     * Test method to generate a QR code for an event ID.
     */
    @Test
    public void generateEventQRCodeTest() {
        // Mock event ID for testing
        String eventId = "123456789";

        // Create a MultiFormatWriter instance for encoding the QR code
        MultiFormatWriter writer = new MultiFormatWriter();

        try {
            // Generate the QR code matrix for the event ID
            BitMatrix matrix = writer.encode(eventId, BarcodeFormat.QR_CODE, 400, 400);

            // Convert the matrix into a Bitmap
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap eventQRBitmap = encoder.createBitmap(matrix);

            // Verify that the generated QR code bitmap is not null
            assertNotNull(eventQRBitmap);

            // Log success message
            Log.d("EventQRCodeTest", "Event QR code generation successful.");

        } catch (WriterException e) {
            // Log error if QR code generation fails
            Log.e("EventQRCodeTest", "Event QR code generation failed: " + e.getMessage());
        }
    }

    /**
     * Test method to generate a QR code for an event description.
     */
    @Test
    public void generateDescriptionQRCodeTest() {
        // Mock event name for testing
        String eventName = "Event12345";

        // Create a MultiFormatWriter instance for encoding the QR code
        MultiFormatWriter writer = new MultiFormatWriter();

        try {
            // Generate the QR code matrix for the event name
            BitMatrix matrix = writer.encode(eventName, BarcodeFormat.QR_CODE, 400, 400);

            // Convert the matrix into a Bitmap
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap descriptionQRBitmap;
            descriptionQRBitmap = encoder.createBitmap(matrix);

            // Verify that the generated QR code bitmap is not null
            assertNotNull(descriptionQRBitmap);

            // Log success message
            Log.d("DescriptionQRCodeTest", "Description QR code generation successful.");

        } catch (WriterException e) {
            // Log error if QR code generation fails
            Log.e("DescriptionQRCodeTest", "Description QR code generation failed: " + e.getMessage());
        }
    }
}


