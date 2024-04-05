package com.example.eventgate;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;

import com.example.eventgate.organizer.CreateAlertFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CreateAlertFragmentTest {
    @Before
    public void setUp() throws InterruptedException {
        // fragment requires data from activity so create a bundle with test data
        Bundle args = new Bundle();
        args.putString("eventId", "lRjkvRKnUmHL4432qLYw");
        // Launch the fragment with the bundle
        FragmentScenario.launchInContainer(CreateAlertFragment.class, args, R.style.Theme_EventGate).moveToState(Lifecycle.State.CREATED);
        // wait for fragment to pop up
        Thread.sleep(2000);
    }
    @Test
    public void testCancelButton() {
        // click cancel button
        Espresso.onView(withId(R.id.create_alert_cancel_button)).perform(ViewActions.click());
        // check that OrganizerEventEditorActivity is now shown
        Espresso.onView(withId(R.id.eventContainer)).check(matches(isDisplayed()));
    }
}
