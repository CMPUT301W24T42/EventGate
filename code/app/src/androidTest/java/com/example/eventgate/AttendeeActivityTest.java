package com.example.eventgate;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.eventgate.attendee.AttendeeActivity;

import org.junit.Rule;
import org.junit.Test;

public class AttendeeActivityTest {

    @Rule
    public ActivityScenarioRule<AttendeeActivity> activityRule =
            new ActivityScenarioRule<>(AttendeeActivity.class);

    @Test
    public void testQRButtonClicked() {
        try (ActivityScenario<AttendeeActivity> scenario = ActivityScenario.launch(AttendeeActivity.class)) {
            // Click on the QR button
            onView(withId(R.id.qr_button)).perform(click());
        }
    }

}
