package com.example.eventgate;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasToString;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testIntent() {
        // tests starting AttendeeActivity and getting back to MainActivity from there
        onView(withId(R.id.attendee_button)).perform(click());  // send to AttendeeActivity
        onView(withText("Cancel")).perform(click());  // dismiss dialog asking for user info
        onView(withId(R.id.attendee_layout)).check(matches(isDisplayed()));  // check that AttendeeActivity is being shown
        onView(withId(R.id.attendee_back_button)).perform(click());  // send back to MainActivity
        onView(withId(R.id.main_activity)).check(matches(isDisplayed()));  // check that MainActivity is being shown

        // tests starting OrganizerMainMenuActivity and getting back to MainActivity from there
        onView(withId(R.id.organizer_button)).perform(click());
        onView(withId(R.id.organizer_menu_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.OrganizerMainMenuBackButton)).perform(click());
        onView(withId(R.id.main_activity)).check(matches(isDisplayed()));

        // tests starting AdminActivity and getting back to MainActivity from there
        onView(withId(R.id.admin_button)).perform(click());
        onView(withId(R.id.admin_activity_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.admin_back_button)).perform(click());
        onView(withId(R.id.main_activity)).check(matches(isDisplayed()));
    }

    @Test
    public void testAdminEventListView() {
        // navigate to AdminActivity to check that the event we will create does not already exist
        onView(withId(R.id.admin_button)).perform(click());
        onData(not("Event for testing")).inAdapterView(withId(R.id.event_list));
        // navigate back to MainActivity
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
        onData(is("Event for testing")).inAdapterView(withId(R.id.event_list));
    }
}
