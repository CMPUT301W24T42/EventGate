package com.example.eventgate.event;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;

import com.example.eventgate.organizer.OrganizerAlert;

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
     * list of alerts associated with the event
     */
    private ArrayList<OrganizerAlert> alerts;

    /**
     * Holds the details of the event
     */
    private String eventDetails;

    /**
     * Holds the attendance limit of the event
     */
    private Integer eventAttendanceLimit;

    /**
     * Holds the attendance limit of the event
     */
    private Boolean geolocationEnabled;

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
     * this sets the geolocationEnabled of the event
     * @param geolocationEnabled whether to enable geolocation tracking
     */
    public void setGeolocation(Boolean geolocationEnabled) {
        this.geolocationEnabled = geolocationEnabled;
    }

    /**
     * this gets whether tracking is enabled
     * @return whether tracking is enabled
     */
    public Boolean getGeolocation() {
        return geolocationEnabled;
    }

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
     * this is used to get the list of alerts associated with an event
     * @return a list of OrganizerAlert objects
     */
    public ArrayList<OrganizerAlert> getAlerts() {
        return alerts;
    }

    public Integer getEventAttendanceLimit() { return eventAttendanceLimit; }
    public void setEventAttendanceLimit(Integer attendanceLimit) {
        this.eventAttendanceLimit = attendanceLimit;
    }

    @NonNull
    @Override
    public String toString() {
        return eventName;
    }
}
