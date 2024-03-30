package com.example.eventgate;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.eventgate.admin.AdminActivity;
import com.example.eventgate.admin.AdminEventViewerActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AdminEventViewerActivityTest {
//    @Rule
//    public ActivityScenarioRule<AdminEventViewerActivity> activityRule =
//            new ActivityScenarioRule<>(AdminEventViewerActivity.class);

    @Before
    public void loadData() throws InterruptedException {
        Thread.sleep(1000);  // wait for any data to load in
    }

    @Test
    public void testBackButton() {
        // Activity requires data from previous activity, so create an intent and put test data
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AdminEventViewerActivity.class);
        // Add data to the intent
        intent.putExtra("eventId", "lzBe9WmFzRZ9CcpC2R1C");
        intent.putExtra("name", "AdminEventViewerActivityTest Event");

        // Launch activity with the intent
        ActivityScenario<AdminEventViewerActivity> scenario = ActivityScenario.launch(intent);
        // click back button
        onView(withId(R.id.admin_event_viewer_back_button)).perform(click());
        // Check if the activity is finished or closed
        scenario.onActivity(activity -> assertTrue(activity.isFinishing()));
    }
}
