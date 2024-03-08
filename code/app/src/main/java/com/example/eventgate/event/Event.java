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
    /**
     * Holds the bitmap for the QR code
     */
    private Bitmap eventQRBitmap;

    /**
     * Constructs an Event
     * @param eventName the name of the event
     */
    public Event(String eventName) {
        this.eventName = eventName;
    }

    /**
     * this get the name of an event
     * @return the name of the event
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * this sets the name of the event
     * @param eventName the name to be set
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * this gets the id of the event
     * @return the id of the event
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * this sets the id for the event
     * @param id the id to be set
     */
    public void setEventId(String id) {
        eventId = id;
    }

    /**
     * this gets the bitmap for the event's QR code
     * @return the QR's bitmap
     */
    public Bitmap getEventQRBitmap() {
        return eventQRBitmap;
    }

    /**
     * this sets the bitmap for the event's QR code
     * @param eventQRBitmap the bitmap of the event's QR code
     */
    public void setEventQRBitmap(Bitmap eventQRBitmap) {
        this.eventQRBitmap = eventQRBitmap;
    }
}
