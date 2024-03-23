package com.example.eventgate;

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
     * this constructs a new attendee object
     * @param name the name of the attendee
     * @param deviceId the firebase installation id of the attendee
     */
    public Attendee(String name, String deviceId) {
        this.name = name;
        this.deviceId = deviceId;
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
}
