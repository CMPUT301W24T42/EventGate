package com.example.eventgate;
import static org.junit.Assert.*;


import static org.junit.Assert.*;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import com.example.eventgate.admin.AdminEventViewerActivity;
import com.example.eventgate.attendee.AttendeeActivity;
import com.example.eventgate.attendee.AttendeeEventViewer;
import com.example.eventgate.attendee.AttendeeViewParticipants;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import java.util.ArrayList;


public class AttendeeDeterministicProfilePicTest {


    private Intent createIntentWithTestData() {


        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AttendeeEventViewer.class);

        return intent;
    }

    @Test
    public void backButtonFinishesActivity() {
        // Launch activity with the intent
        ActivityScenario<AttendeeActivity> scenario = ActivityScenario.launch(createIntentWithTestData());
        // click back button
        onView(withId(R.id.attendee_back_button)).perform(click());
        // Check if the activity is finished or closed
        scenario.onActivity(activity -> assertTrue(activity.isFinishing()));
    }

    @Test
    public void createBitmapTest() {
        // Launch activity with the intent
        ActivityScenario<AttendeeActivity> scenario = ActivityScenario.launch(createIntentWithTestData());
        // create bitmap
        scenario.onActivity(activity -> {
            //setup test info
            String userId = "l5bEt4ADFRKnUmHLty46";
            activity.hashBytes = userId.getBytes();
            activity.createBitmap2();


            Bitmap profileBitmap = activity.profileBitmap;
            // Check that the bitmap is not null
            assertNotNull("Bitmap should not be null after createBitmap2()", profileBitmap);
        });
    }




}
