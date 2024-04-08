package com.example.eventgate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.example.eventgate.event.Event;

import org.junit.Test;

import java.util.ArrayList;

public class EventTest {
    /**
     * create a mock Event used in tests
     * @return an Event object
     */
    public Event mockEvent() {
        return new Event("mockEvent");
    }

    /**
     * test that a new event has initialized fields
     */
    @Test
    public void testNull() {
        Event mock = mockEvent();
        assertNull(mock.getEventId());
        assertNull(mock.getEventDetails());
        assertNull(mock.getEventAttendanceLimit());
        assertEquals(new ArrayList<>(), mock.getAlerts());  // should be empty list
    }

    /**
     * tests getter and setter methods for eventDetails
     */
    @Test
    public void testEventId() {
        Event mock = mockEvent();
        assertNull(mock.getEventId());

        // string literals
        mock.setEventId("randomEventId");
        assertEquals("randomEventId", mock.getEventId());

        // string objects
        String eventId = "someEventId";
        mock.setEventId(eventId);
        assertEquals(eventId, mock.getEventId());
    }

    /**
     * tests getter and setter for EventAttendanceLimit
     */
    @Test
    public void testEventAttendanceLimit() {
        Event mock = mockEvent();
        assertNull(mock.getEventAttendanceLimit());

        // test with positive number
        mock.setEventAttendanceLimit(3);
        assertEquals(Integer.valueOf(3), mock.getEventAttendanceLimit());

        // test with -1 (represents infinite limit of attendees)
        mock.setEventAttendanceLimit(-1);
        assertEquals(Integer.valueOf(-1), mock.getEventAttendanceLimit());
    }
}
