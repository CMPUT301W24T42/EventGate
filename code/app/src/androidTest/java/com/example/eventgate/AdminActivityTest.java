package com.example.eventgate;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.eventgate.admin.AdminActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AdminActivityTest {
    @Rule
    public ActivityScenarioRule<AdminActivity> activityRule =
            new ActivityScenarioRule<>(AdminActivity.class);

    @Before
    public void loadData() throws InterruptedException {
        Thread.sleep(1000); // allow time for the firebase data to load in
    }

    @Test
    public void testBackButton() {
        onView(withId(R.id.admin_back_button)).perform(click());
        // Check if the activity is finished or closed
        activityRule.getScenario().onActivity(activity -> assertTrue(activity.isFinishing()));
    }

    @Test
    public void testEventsListView() {
        // click on the first item in the events list view
        onData(anything())
                .inAdapterView(withId(R.id.event_list))
                .atPosition(0)
                .perform(click());

        // check that it starts the AdminEventViewerActivity
        onView(withId(R.id.admin_event_viewer_layout)).check(matches(isDisplayed()));
    }
}
