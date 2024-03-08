package com.example.eventgate.organizer;

/**
 * This represents an alert that organizers can send
 */
public class OrganizerAlert {
    /**
     * the message that is to be sent
     */
    private String message;

    /**
     * Creates a new OrganizerAlert object
     * @param message the message that will be sent
     */
    public OrganizerAlert(String message) {
        this.message = message;
    }
}
