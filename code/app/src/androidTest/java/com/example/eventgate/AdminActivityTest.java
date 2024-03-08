package com.example.eventgate;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.eventgate.admin.AdminActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminActivityTest {
    @Rule
    public ActivityScenarioRule<AdminActivity> activityRule =
            new ActivityScenarioRule<>(AdminActivity.class);

    @Test
    public void testEventListView() {
            // return to MainActivity
            onView(withId(R.id.admin_back_button)).perform(click());
            // navigate to OrganizerMainMenuActivity so that we can create a test event
            onView(withId(R.id.organizer_button)).perform(click());
            // create an event
            onView(withId(R.id.CreateEventButton)).perform(click());
            // create a name for the event
            onView(withId(R.id.organizerCreateEventName)).perform(ViewActions.typeText("Event for testing"));
            // generate qr codes for the event
            onView(withId(R.id.generateQRButton)).perform(click());
            onView(withId(R.id.generateDescriptionQRButton)).perform(click());
            // finish creating the event
            onView(withId(R.id.organizerCreateEventContinueButton)).perform(click());
            // return to MainActivity and then go to AdminActivity
            onView(withId(R.id.OrganizerMainMenuBackButton)).perform(click());
            onView(withId(R.id.admin_button)).perform(click());
            // check if there is text within the listview that is the name of the created event
            onData(is(instanceOf(String.class))).inAdapterView(withId(R.id.event_list
            )).check(matches(withText(("Event for testing"))));

    }
}
