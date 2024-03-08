package com.example.eventgate;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.eventgate.organizer.OrganizerMainMenuActivity;

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
}
