package com.example.eventgate;

import androidx.test.espresso.UiController;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.ViewAction;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.view.View;
import android.widget.Button;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void dismissDialog() {
        // dismiss the notification popup before every test
        onView(withId(R.id.permission_info)).check(matches(isDisplayed()));
        onView(withId(R.id.no_thanks_button)).perform(click());
        onView(withId(R.id.main_activity)).check(matches(isDisplayed()));
    }

    @Test
    public void testAttendeeButton() {
        // tests starting AttendeeActivity
        onView(withId(R.id.attendee_button)).perform(click());
        onView(withId(R.id.attendee_layout)).check(matches(isDisplayed()));
    }

    @Test
    public void testOrganizerButton() {
        // tests starting OrganizerMainMenuActivity
        onView(withId(R.id.organizer_button)).perform(click());
        onView(withId(R.id.organizer_menu_layout)).check(matches(isDisplayed()));
    }

    @Test
    public void testAdminButton() throws InterruptedException {
        // assume device used for test is an admin and make the admin button visible
        onView(withId(R.id.admin_button)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(Button.class);
            }

            @Override
            public String getDescription() {
                return "Set view visibility";
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.setVisibility(View.VISIBLE);
            }
        });
        // wait for button to show
        Thread.sleep(3000);
        // test starting AdminActivity
        onView(withId(R.id.admin_button)).perform(click());
        onView(withId(R.id.admin_activity_layout)).check(matches(isDisplayed()));
    }


}
