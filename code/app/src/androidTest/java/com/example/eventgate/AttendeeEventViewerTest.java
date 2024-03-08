package com.example.eventgate;

import static org.junit.Assert.*;
import android.app.Activity;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import com.example.eventgate.attendee.AttendeeEventViewer;
import com.example.eventgate.attendee.AttendeeViewParticipants;

import org.junit.Rule;
import org.junit.Test;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class AttendeeEventViewerTest {
    @Rule
    public ActivityScenarioRule<AttendeeEventViewer> activityRule =
            new ActivityScenarioRule<>(AttendeeEventViewer.class);

    @Test
    public void backButtonFinishesActivity() {
        onView(withId(R.id.attendee_back_button)).perform(click());

        //check that we're back in attendee main
        onView(withId(R.id.qr_button)).check(matches(isDisplayed()));
    }

    @Test
    public void viewAttendeesButtonStartsNewActivity() {
        onView(withId(R.id.attendeeViewParticipantsButton)).perform(click());

    }
}