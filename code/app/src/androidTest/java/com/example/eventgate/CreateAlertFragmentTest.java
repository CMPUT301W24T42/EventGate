package com.example.eventgate;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;

import com.example.eventgate.organizer.CreateAlertFragment;
import com.example.eventgate.organizer.OrganizerEventEditorActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CreateAlertFragmentTest {
    @Before
    public void setUp() {
        // fragment requires data from activity so create a bundle with test data
        Bundle args = new Bundle();
        args.putString("eventId", "lRjkvRKnUmHL4432qLYw");
        // Launch the fragment with the bundle
        FragmentScenario.launch(CreateAlertFragment.class, args, R.style.Theme_EventGate);
    }
    @Test
    public void testCancelButton() {
        // click cancel button
        Espresso.onView(withText("Cancel")).perform(ViewActions.click());
        // check that OrganizerEventEditorActivity is now shown
        Espresso.onView(withId(R.id.eventContainer)).check(matches(isDisplayed()));
    }
}
