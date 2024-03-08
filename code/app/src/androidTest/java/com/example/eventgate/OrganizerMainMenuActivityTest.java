package com.example.eventgate;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.eventgate.organizer.OrganizerMainMenuActivity;

import org.junit.Rule;
import org.junit.Test;

public class OrganizerMainMenuActivityTest {
    @Rule
    public ActivityScenarioRule<OrganizerMainMenuActivity> activityRule =
            new ActivityScenarioRule<>(OrganizerMainMenuActivity.class);

    @Test
    public void testCreateEventButtonClicked() {
        try (ActivityScenario<OrganizerMainMenuActivity> scenario = ActivityScenario.launch(OrganizerMainMenuActivity.class)) {
            // Click on the Create Event Button
            onView(withId(R.id.CreateEventButton)).perform(click());
        }
    }

    @Test
    public void testBackButtonClicked() {
        try (ActivityScenario<OrganizerMainMenuActivity> scenario = ActivityScenario.launch(OrganizerMainMenuActivity.class)) {
            onView(withId(R.id.OrganizerMainMenuBackButton)).perform(click());
        }
    }
}