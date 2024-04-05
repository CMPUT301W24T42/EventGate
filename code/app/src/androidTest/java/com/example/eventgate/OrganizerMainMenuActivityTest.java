package com.example.eventgate;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import com.example.eventgate.organizer.OrganizerMainMenuActivity;

import org.junit.Rule;
import org.junit.Test;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.action.ViewActions;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

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

    /**
     * Espresso test method to verify that an event called "Event1" is added.
     */
    @Test
    public void testAddEventCalledEvent1() {
        try (ActivityScenario<OrganizerMainMenuActivity> scenario = ActivityScenario.launch(OrganizerMainMenuActivity.class)) {
            // Click on the Create Event Button
            onView(withId(R.id.CreateEventButton)).perform(click());

            // Enter the event name "Event1"
            onView(withId(R.id.organizerCreateEventName)).perform(ViewActions.typeText("Event1"), ViewActions.closeSoftKeyboard());

            // Generate both QR Codes
            onView(withId(R.id.generateCheckInQRButton)).perform(click());
            onView(withId(R.id.generateDescriptionQRButton)).perform(click());

            // Click on Continue Button
            onView(withId(R.id.organizerCreateEventContinueButton)).perform(click());

            // Verify that the event "Event1" is added to the list
            onView(withText("Event1")).check(matches(isDisplayed()));
        }
    }
}