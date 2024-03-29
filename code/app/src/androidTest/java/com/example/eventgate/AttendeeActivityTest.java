package com.example.eventgate;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
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
        // Click on the QR button
        onView(withId(R.id.qr_button)).perform(click());
    }

    @Test
    public void testProfileImageButtonOpensDialog() {
        onView(withId(R.id.profile_image)).perform(click());
        onView(withText("Upload New Profile Picture")).check(matches(isDisplayed()));
    }

    @Test
    public void testSettingsButtonOpensDialog() {
        onView(withId(R.id.settings_button)).perform(click());
        onView(withText("Save")).check(matches(isDisplayed()));
    }

    @Test
    public void testRegisteredEventsDialog() {
        onView(withId(R.id.registeredEventsButton)).perform(click());
        onView(withText("My Registered Events")).check(matches(isDisplayed()));
    }

    @Test
    public void testMainActivityIsOpenedOnBackPress() {
        onView(withId(R.id.attendee_back_button)).perform(click());

        onView(withId(R.id.attendee_button)).check(matches(isDisplayed()));
    }

}
