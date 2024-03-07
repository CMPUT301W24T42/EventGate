package com.example.eventgate.event;

import android.graphics.Bitmap;

public class Event {
    /**
     * Holds the document id for the event
     */
    private String eventId;
    /**
     * Holds the name of the event
     */
    private String eventName;
    private Bitmap eventQRBitmap;

    /**
     * Constructs an Event
     * @param eventName the name of the event
     */
    public Event(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String id) {
        eventId = id;
    }

    public Bitmap getEventQRBitmap() {
        return eventQRBitmap;
    }

    public void setEventQRBitmap(Bitmap eventQRBitmap) {
        this.eventQRBitmap = eventQRBitmap;
    }
}
