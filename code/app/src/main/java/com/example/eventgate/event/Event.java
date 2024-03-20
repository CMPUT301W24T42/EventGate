package com.example.eventgate.event;

import android.graphics.Bitmap;
import android.os.Parcelable;

import com.example.eventgate.organizer.OrganizerAlert;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * this class represents an event
 * it allows users to get and set info regarding the event
 */
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
    /**
     * list of alerts associated with the event
     */
    private ArrayList<OrganizerAlert> alerts;
    /**
     * Holds the details of the event
     */
    private String eventDetails;

    /**
     * Constructs an Event
     * @param eventName the name of the event
     */
    public Event(String eventName) {
        this.eventName = eventName;
        this.alerts = new ArrayList<>();
    }

    /**
     * this get the name of an event
     * @return the name of the event
     */
    public String getEventDetails() {
        return eventDetails;
    }

    public void setEventDetails(String eventDetails) {
        this.eventDetails = eventDetails;
    }

    /**
     * this gets the name of the event
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

    public String getEventDescription() { return eventDescription; }
  
    public void setEventDescription(String eventDescription) { this.eventDescription = eventDescription; }

    /**
     * this is used to get the id of an event
     * @return a string representing an event's id
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * this sets the id for the event
     * @param id the id to be set
     */
    public void setEventId(String id) {
        this.eventId = id;
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

    public Bitmap getDescriptionQRBitmap() {
        return descriptionQRBitmap;
    }

    public void setDescriptionQRBitmap(Bitmap descriptionQRBitmap) {
        this.descriptionQRBitmap = descriptionQRBitmap;
    }

    /**
     * this is used to get the list of alerts associated with an event
     * @return a list of OrganizerAlert objects
     */
    public ArrayList<OrganizerAlert> getAlerts() {
        return alerts;
    }
}
