package com.example.eventgate;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;

import com.example.eventgate.organizer.OrganizerEventEditorActivity;
import com.example.eventgate.organizer.OrganizerMainMenuActivity;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onData;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventgate.event.Event;

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
     * Test to verify that clicking on an item in the event list opens the event editor activity
     * with the correct event ID and name as extras in the intent.
     */
    @Test
    public void testEventListItemClicked_OpensEventEditorActivity() {
        // Click on the first item in the event list
        onData(is(instanceOf(Event.class)))
                .inAdapterView(withId(R.id.EventListView))
                .atPosition(0)
                .perform(click());

        // Verify that the intent to open the event editor activity is sent with correct extras
        Intents.intended(IntentMatchers.hasComponent(OrganizerEventEditorActivity.class.getName()));
        Intents.intended(IntentMatchers.hasExtra("eventId", "AtvaEq6KTuDcEgim7URQ")); // This is where you enter the expected eventId
        Intents.intended(IntentMatchers.hasExtra("eventName", "Event 1")); // This is where you enter the expected eventName
    }
}