package com.example.eventgate;

import static org.junit.Assert.assertEquals;

import com.example.eventgate.attendee.Attendee;

import org.junit.Test;
import org.junit.Before;

public class AttendeeTest {

    private Attendee attendee;

    @Before
    public void setUp() {
        attendee = new Attendee("John Doe", "firebase-installation-id", "attendee-document-id");
    }

    @Test
    public void testGetName() {
        assertEquals("John Doe", attendee.getName());
    }

    @Test
    public void testSetName() {
        attendee.setName("Jane Doe");
        assertEquals("Jane Doe", attendee.getName());
    }

    @Test
    public void testGetDeviceId() {
        assertEquals("firebase-installation-id", attendee.getDeviceId());
    }

    @Test
    public void testGetAttendeeId() {
        assertEquals("attendee-document-id", attendee.getAttendeeId());
    }

    @Test
    public void testGetHomepage() {
        assertEquals("", attendee.getHomepage());
    }

    @Test
    public void testSetHomepage() {
        attendee.setHomepage("https://example.com");
        assertEquals("https://example.com", attendee.getHomepage());
    }

    @Test
    public void testGetEmail() {
        assertEquals("", attendee.getEmail());
    }

    @Test
    public void testSetEmail() {
        attendee.setEmail("john@example.com");
        assertEquals("john@example.com", attendee.getEmail());
    }

    @Test
    public void testGetPhoneNumber() {
        assertEquals("", attendee.getPhoneNumber());
    }

    @Test
    public void testSetPhoneNumber() {
        attendee.setPhoneNumber("123456789");
        assertEquals("123456789", attendee.getPhoneNumber());
    }

    @Test
    public void testGetProfilePicture() {
        assertEquals("", attendee.getProfilePicture());
    }

    @Test
    public void testSetProfilePicture() {
        attendee.setProfilePicture("https://example.com/profile.jpg");
        assertEquals("https://example.com/profile.jpg", attendee.getProfilePicture());
    }
}
