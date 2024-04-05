package com.example.eventgate;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;

import com.example.eventgate.organizer.OrganizerCreateEventFragment;

import org.junit.Test;

import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Instrumented test class for testing the behavior of OrganizerCreateEventFragment.
 * Outstanding issues: Cannot get the test to open the fragment.
 */
public class OrganizerCreateEventFragmentTest {

    /**
     * Test to verify that clicking on the Continue Button triggers the expected behavior.
     */
    @Test
    public void testContinueButtonClicked() {
        FragmentScenario.launchInContainer(OrganizerCreateEventFragment.class);

        // Click on the Continue Button
        Espresso.onView(withId(R.id.organizerCreateEventContinueButton)).perform(ViewActions.click());
    }

    /**
     * Test to verify that clicking on the Cancel Button triggers the expected behavior.
     */
    @Test
    public void testCancelButtonClicked() {
        FragmentScenario.launchInContainer(OrganizerCreateEventFragment.class);

        // Click on the Cancel Button
        Espresso.onView(withId(R.id.organizerCreateEventCancelButton)).perform(ViewActions.click());
    }

    /**
     * Test to verify that clicking on the Generate QR Button works.
     */
    @Test
    public void testGenerateQRButtonClicked() {
        FragmentScenario<OrganizerCreateEventFragment> scenario = FragmentScenario.launchInContainer(OrganizerCreateEventFragment.class);

        // Click on the Generate QR Button
            Espresso.onView(withId(R.id.generateCheckInQRButton)).perform(ViewActions.click());
    }
    /**
     * Test to verify that clicking on the Generate Description QR Button works.
     */

    @Test
    public void testGenerateDescriptionQRButtonClicked() {
        FragmentScenario<OrganizerCreateEventFragment> scenario = FragmentScenario.launchInContainer(OrganizerCreateEventFragment.class);

        // Click on the Generate Description QR Button
        Espresso.onView(withId(R.id.generateDescriptionQRButton)).perform(ViewActions.click());

    }
}

