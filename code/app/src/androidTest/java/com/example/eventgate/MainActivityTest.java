package com.example.eventgate;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

public class MainActivityTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testAttendeeButton() {
        // tests starting AttendeeActivity
        onView(withId(R.id.attendee_button)).perform(click());
        onView(withId(R.id.attendee_layout)).check(matches(isDisplayed()));
    }

    @Test
    public void testOrganizerButton() {
        // tests starting OrganizerMainMenuActivity
        onView(withId(R.id.organizer_button)).perform(click());
        onView(withId(R.id.organizer_menu_layout)).check(matches(isDisplayed()));
    }


}
