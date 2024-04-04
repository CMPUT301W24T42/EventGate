package com.example.eventgate.attendee;

import java.io.Serializable;

/**
 * this class represents a attendee
 * it allows one to set and get attendee info
 */
public class Attendee implements Serializable {
    /**
     * this holds the name of a attendee
     */
    private String name;
    /**
     * this holds the firebase installation id of the attendee
     */
    private String deviceId;
    /**
     * this holds the id of the firestore collection document for the attendee
     */
    private String attendeeId;
    private String homepage;
    private String email;
    private String phoneNumber;

    /**
     * this constructs a new attendee object
     * @param name the name of the attendee
     * @param deviceId the firebase installation id of the attendee
     */
    public Attendee(String name, String deviceId, String attendeeId) {
        this.name = name;
        this.deviceId = deviceId;
        this.attendeeId = attendeeId;
        this.homepage = "";
        this.email = "";
        this.phoneNumber = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAttendeeId() {
        return attendeeId;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
