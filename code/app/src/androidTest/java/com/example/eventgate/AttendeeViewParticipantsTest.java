package com.example.eventgate;

import static org.junit.Assert.*;

import org.junit.Test;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
public class AttendeeViewParticipantsTest {

    @Test
    public void backButtonFinishesActivity() {

        onView(withId(R.id.attendee_back_button)).perform(click());

    }
}