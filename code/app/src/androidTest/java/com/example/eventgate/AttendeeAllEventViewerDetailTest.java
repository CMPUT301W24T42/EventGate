package com.example.eventgate;

import static androidx.test.espresso.action.ViewActions.click;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.widget.Button;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import com.example.eventgate.attendee.AttendeeActivity;
import org.junit.Rule;
import org.junit.Test;
import androidx.test.espresso.action.ViewActions;

import com.example.eventgate.attendee.AttendeeAllEventViewerDetail;
import com.example.eventgate.attendee.AttendeeViewParticipants;

import org.junit.Test;

public class AttendeeAllEventViewerDetailTest {


    @Test
    public void viewPagerSwipeTest() {

        onView(withId(R.id.viewPager)).check(matches(isDisplayed()));


        onView(withId(R.id.viewPager)).perform(ViewActions.swipeRight());


    }

    @Test
    public void testViewAttendeesButtonClick() {
        onView(withId(R.id.attendeeViewParticipantsButton)).perform(click());

    }
}
