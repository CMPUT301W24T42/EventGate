package com.example.eventgate.event;

import android.graphics.Bitmap;

import com.example.eventgate.organizer.OrganizerAlert;

import java.util.ArrayList;

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
     * Holds the event description
     */
    private String eventDescription;
    /**
     * Holds the Bitmap of the check in QR Code.
     */
    private Bitmap eventQRBitmap;
    /**
     * Holds the Bitmap of the event description QR Code.
     */
    private Bitmap descriptionQRBitmap;
    private ArrayList<OrganizerAlert> alerts;

    /**
     * Constructs an Event
     * @param eventName the name of the event
     */
    public Event(String eventName) {
        this.eventName = eventName;
        this.alerts = new ArrayList<>();
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    public String getEventDescription() { return eventDescription; }
    public void setEventDescription(String eventDescription) { this.eventDescription = eventDescription; }
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String id) {
        this.eventId = id;
    }

    public Bitmap getEventQRBitmap() {
        return eventQRBitmap;
    }

    public void setEventQRBitmap(Bitmap eventQRBitmap) {
        this.eventQRBitmap = eventQRBitmap;
    }

    public Bitmap getDescriptionQRBitmap() {
        return descriptionQRBitmap;
    }

    public void setDescriptionQRBitmap(Bitmap descriptionQRBitmap) {
        this.descriptionQRBitmap = descriptionQRBitmap;
    }

    public ArrayList<OrganizerAlert> getAlerts() {
        return alerts;
    }
}
