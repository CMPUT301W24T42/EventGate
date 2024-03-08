package com.example.eventgate;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;

import com.example.eventgate.organizer.OrganizerCreateEventFragment;

import org.junit.Test;

import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class OrganizerCreateEventFragmentTest {

    @Test
    public void testContinueButtonClicked() {
        FragmentScenario.launchInContainer(OrganizerCreateEventFragment.class);

        // Click on the Continue Button
        Espresso.onView(withId(R.id.organizerCreateEventContinueButton)).perform(ViewActions.click());
    }

    @Test
    public void testCancelButtonClicked() {
        FragmentScenario.launchInContainer(OrganizerCreateEventFragment.class);

        // Click on the Cancel Button
        Espresso.onView(withId(R.id.organizerCreateEventCancelButton)).perform(ViewActions.click());
    }
}

