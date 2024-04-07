package com.example.eventgate;

import static org.junit.Assert.*;
import android.app.Activity;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.eventgate.admin.AdminEventViewerActivity;
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

public class AttendeeEventViewerTest {

    private Intent createIntentWithTestData() {
        // Activity requires data from previous activity, so create an intent and put test data
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AttendeeEventViewer.class);
        // Add data to the intent
        intent.putExtra("EventID", "lRjkvRKnUmHL4432qLYw");
        intent.putExtra("EventName", "AttendeeEventViewer Event");
        intent.putExtra("alerts", new ArrayList<>());
        return intent;
    }

    @Test
    public void backButtonFinishesActivity() {
        // Launch activity with the intent
        ActivityScenario<AttendeeEventViewer> scenario = ActivityScenario.launch(createIntentWithTestData());
        // click back button
        onView(withId(R.id.attendee_back_button)).perform(click());
        // Check if the activity is finished or closed
        scenario.onActivity(activity -> assertTrue(activity.isFinishing()));
    }

    @Test
    public void viewAttendeesButtonStartsNewActivity() {
        // Launch activity with the intent
        ActivityScenario.launch(createIntentWithTestData());
        // click view participants button
        onView(withId(R.id.attendeeViewParticipantsButton)).perform(click());
        // check that view participants layout is being shown after click
        onView(withId(R.id.attendee_view_participants_layout)).check(matches(isDisplayed()));  //
    }
}