package com.example.eventgate;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventgate.organizer.OrganizerMainMenuActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test class for testing the behavior of OrganizerMainMenuActivity.
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerMainMenuActivityTest {

    @Rule
    public IntentsTestRule<OrganizerMainMenuActivity> activityRule =
            new IntentsTestRule<>(OrganizerMainMenuActivity.class);

    /**
     * Test to verify that clicking on the "Create Event" button triggers the expected behavior.
     */
    @Test
    public void testCreateEventButtonClicked() {
        try (ActivityScenario<OrganizerMainMenuActivity> scenario = ActivityScenario.launch(OrganizerMainMenuActivity.class)) {
            // Click on the Create Event Button
            onView(withId(R.id.CreateEventButton)).perform(click());
        }
    }

    /**
     * Test to verify that clicking on the back button triggers the expected behavior.
     */
    @Test
    public void testBackButtonClicked() {
        try (ActivityScenario<OrganizerMainMenuActivity> scenario = ActivityScenario.launch(OrganizerMainMenuActivity.class)) {
            onView(withId(R.id.OrganizerMainMenuBackButton)).perform(click());
        }
    }
}
