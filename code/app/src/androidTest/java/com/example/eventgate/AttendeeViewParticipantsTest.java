package com.example.eventgate;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.eventgate.admin.AdminActivity;
import com.example.eventgate.admin.AdminEventViewerActivity;
import com.example.eventgate.attendee.AttendeeViewParticipants;

public class AttendeeViewParticipantsTest {

    private Intent createIntentWithTestData() {
        // Activity requires data from previous activity, so create an intent and put test data
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AttendeeViewParticipants.class);
        // Add data to the intent
        intent.putExtra("eventID", "apH6WntRW7mG4W6j9zXO");
        intent.putExtra("eventName", "AdminEventViewerActivityTest Event");
        return intent;
    }

    @Rule
    public ActivityScenarioRule<AttendeeViewParticipants> activityRule =
            new ActivityScenarioRule<>(createIntentWithTestData());

    @Test
    public void backButtonFinishesActivity() {
        // click back button
        onView(withId(R.id.attendee_back_button)).perform(click());
        // Check if the activity is finished/closed
        activityRule.getScenario().onActivity(activity -> assertTrue(activity.isFinishing()));
    }
}