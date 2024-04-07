package com.example.eventgate;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.eventgate.admin.AdminActivity;
import com.example.eventgate.attendee.AttendeeViewParticipants;

public class AttendeeViewParticipantsTest {
    @Rule
    public ActivityScenarioRule<AttendeeViewParticipants> activityRule =
            new ActivityScenarioRule<>(AttendeeViewParticipants.class);
    @Test
    public void backButtonFinishesActivity() {
        // click back button
        onView(withId(R.id.attendee_back_button)).perform(click());
        // Check if the activity is finished/closed
        activityRule.getScenario().onActivity(activity -> assertTrue(activity.isFinishing()));
    }
}