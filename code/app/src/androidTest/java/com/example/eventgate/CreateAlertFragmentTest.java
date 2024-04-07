package com.example.eventgate;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.eventgate.organizer.OrganizerEventEditorActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class CreateAlertFragmentTest {
    private View decorView;

    private Intent createIntentWithTestData() {
        // Activity requires data from previous activity, so create an intent and put test data
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), OrganizerEventEditorActivity.class);
        // Add data to the intent
        intent.putExtra("eventId", "lRjkvRKnUmHL4432qLYw");
        intent.putExtra("eventName", "AttendeeEventViewer Event");
        return intent;
    }

    @Rule
    public ActivityScenarioRule<OrganizerEventEditorActivity> activityRule =
            new ActivityScenarioRule<>(createIntentWithTestData());

    @Before
    public void setUp() {
        // get the decorview
        activityRule.getScenario().onActivity(activity -> decorView = activity.getWindow().getDecorView());
    }

    @Test
    public void testCancelButton() {
        // launch dialog fragment
        Espresso.onView(withText("Create Alert")).perform(ViewActions.click());
        // click cancel button
        Espresso.onView(withText("Cancel")).perform(ViewActions.click());
        // check that the CreateAlertDialog is no longer being shown
        Espresso.onView(withId(R.id.eventContainer)).check(matches(isDisplayed()));

    }

//    @Test
//    public void testNoAlertTitleEntered() throws InterruptedException {
//        // launch dialog fragment
//        Espresso.onView(withText("Create Alert")).perform(ViewActions.click());
//        // click send button without a title or message
//        Espresso.onView(withText("Send")).perform(ViewActions.click());
//        // check that there is a toast prompting the user to enter a title
//        Espresso.onView(withText("Please"))
//                .inRoot(withDecorView(not(decorView)))
//                .check(matches(isDisplayed()));
//
//    }
}
