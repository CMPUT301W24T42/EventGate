package com.example.eventgate;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.eventgate.organizer.OrganizerEventEditorActivity;

import org.junit.Rule;
import org.junit.Test;

public class OrganizerEditEventActivityTest {

    @Rule
    public ActivityScenarioRule<OrganizerEventEditorActivity> activityRule =
            new ActivityScenarioRule<>(OrganizerEventEditorActivity.class);

    @Test
    public void testEventNamePropagation() {
        String eventName = "Made Up Event";
        String eventId = "EJtxq5hxlRH2lALrpQOC";

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), OrganizerEventEditorActivity.class);
        intent.putExtra("eventName", eventName);
        intent.putExtra("eventId", eventId);

        try (ActivityScenario<OrganizerEventEditorActivity> scenario = ActivityScenario.launch(intent)) {
            // Verify that the eventName propagates correctly
            onView(withId(R.id.EventListViewTitle)).check(matches(withText(eventName)));

            // Verify that the back button is displayed
            onView(withId(R.id.OrganizerEditBackButton)).check(matches(isDisplayed()));

            // Click the back button
            onView(withId(R.id.OrganizerEditBackButton)).perform(click());
        }
    }

}
