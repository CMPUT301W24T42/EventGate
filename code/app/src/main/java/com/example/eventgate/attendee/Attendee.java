package com.example.eventgate.attendee;

/**
 * this class represents a attendee
 * it allows one to set and get attendee info
 */
public class Attendee {
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

    /**
     * this constructs a new attendee object
     * @param name the name of the attendee
     * @param deviceId the firebase installation id of the attendee
     */
    public Attendee(String name, String deviceId, String attendeeId) {
        this.name = name;
        this.deviceId = deviceId;
        this.attendeeId = attendeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAttendeeId() {
        return attendeeId;
    }

    public void setAttendeeId(String attendeeId) {
        this.attendeeId = attendeeId;
    }
}
