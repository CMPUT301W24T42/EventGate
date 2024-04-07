package com.example.eventgate;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.SmallTest;

import com.example.eventgate.admin.AdminEventViewerActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AdminEventViewerActivityTest {
    private Intent createIntentWithTestData() {
        // Activity requires data from previous activity, so create an intent and put test data
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AdminEventViewerActivity.class);
        // Add data to the intent
        intent.putExtra("eventId", "apH6WntRW7mG4W6j9zXO");
        intent.putExtra("name", "AdminEventViewerActivityTest Event");
        return intent;
    }

    @Rule
    public ActivityScenarioRule<AdminEventViewerActivity> activityScenarioRule = new ActivityScenarioRule<>(createIntentWithTestData());

    @Before
    public void setUp() throws InterruptedException {
        Thread.sleep(1000);  // wait for any data to load in
    }

    @Test
    public void testBackButton() {
        // get scenario
        ActivityScenario<AdminEventViewerActivity> scenario = activityScenarioRule.getScenario();
        // click back button
        onView(withId(R.id.admin_event_viewer_back_button)).perform(click());
        // Check if the activity is finished or closed
        scenario.onActivity(activity -> assertTrue(activity.isFinishing()));
    }

    @Test
    public void testDeleteButton() {
        // click button to delete poster
        onView(withId(R.id.delete_poster_button)).perform(click());
        // check that a dialog pops up confirming the user wants to delete
        onView(withId(R.id.confirm_delete_layout)).check(matches(isDisplayed()));
    }
}
