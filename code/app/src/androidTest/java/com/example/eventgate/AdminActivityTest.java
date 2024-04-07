package com.example.eventgate;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.eventgate.admin.AdminActivity;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AdminActivityTest {
    @Rule
    public ActivityScenarioRule<AdminActivity> activityRule =
            new ActivityScenarioRule<>(AdminActivity.class);

    @Test
    public void testBackButton() {
        onView(withId(R.id.admin_back_button)).perform(click());
        // Check if the activity is finished or closed
        activityRule.getScenario().onActivity(activity -> assertTrue(activity.isFinishing()));
    }

    @Test
    public void testEventsListView() throws InterruptedException {
        Thread.sleep(1000); // allow time for the firebase data to load in
        // click on the first item in the events list view
        onData(anything())
                .inAdapterView(withId(R.id.event_list))
                .atPosition(0)
                .perform(click());

        // check that it starts the AdminEventViewerActivity
        onView(withId(R.id.admin_event_viewer_layout)).check(matches(isDisplayed()));
    }

    @Test
    public void testViewPagerSwipe() {
        // swipe left to the users tab
        onView(allOf(withId(R.id.admin_viewpager), isDisplayed())).perform(swipeLeft());
        onView(withId(R.id.users_layout)).check(matches(isDisplayed())); // check we're seeing users tab
        // swipe left to the images tab
        onView(allOf(withId(R.id.admin_viewpager), isDisplayed())).perform(swipeLeft());
        onView(withId(R.id.image_grid_view)).check(matches(isDisplayed())); // check we're seeing images tab
    }

    @Test
    public void testAttendeesListView() throws InterruptedException {
        // swipe left to the users tab
        onView(allOf(withId(R.id.admin_viewpager), isDisplayed())).perform(swipeLeft());
        Thread.sleep(1000);  // give time for data to load in
        // click on the first item in the users list view
        onData(anything())
                .inAdapterView(withId(R.id.user_list))
                .atPosition(0)
                .perform(click());
        // check that it shows a UserInfoDialog
        onView(withId(R.id.user_info_popup_layout));
    }

    @Test
    public void testImagesGridView() throws InterruptedException {
        // swipe left twice to the images tab
        onView(allOf(withId(R.id.admin_viewpager), isDisplayed())).perform(swipeLeft());
        onView(allOf(withId(R.id.admin_viewpager), isDisplayed())).perform(swipeLeft());
        Thread.sleep(1000);  // give time for data to load in
        // click on the first item in the images grid view
        onData(anything())
                .inAdapterView(withId(R.id.image_grid_view))
                .atPosition(0)
                .perform(click());
        // check that it shows an ImagePopUpDialog
        onView(withId(R.id.image_popup_layout));
    }
}
