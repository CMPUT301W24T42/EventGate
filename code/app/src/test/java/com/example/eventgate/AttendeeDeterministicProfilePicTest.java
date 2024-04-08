package com.example.eventgate;
import static org.junit.Assert.*;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.example.eventgate.attendee.AttendeeActivity;
import org.junit.Test;


public class AttendeeDeterministicProfilePicTest {

    @Test
    public void testCreateBitmap2() {

        AttendeeActivity attendeeInstance = new AttendeeActivity();

        // create bitmap
        attendeeInstance.createBitmap2();


        Bitmap profileBitmap = attendeeInstance.profileBitmap;

        // check that the bitmap is not null
        assertNotNull("Bitmap should not be null after createBitmap2()", profileBitmap);
    }
}
