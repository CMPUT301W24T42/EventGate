package com.example.eventgate;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.eventgate.admin.AdminActivity;

import org.junit.Rule;
import org.junit.Test;

public class AdminEventViewerActivityTest {
    @Rule
    public ActivityScenarioRule<AdminActivity> activityRule =
            new ActivityScenarioRule<>(AdminActivity.class);

    @Test
    public void testBackButton() {
        onView(withId(R.id.admin_event_viewer_back_button)).perform(click());
        // Check if the activity is finished or closed
        activityRule.getScenario().onActivity(activity -> assertTrue(activity.isFinishing()));
    }
}
