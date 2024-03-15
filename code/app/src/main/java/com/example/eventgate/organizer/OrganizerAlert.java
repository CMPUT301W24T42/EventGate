package com.example.eventgate.organizer;

/**
 * This represents an alert that organizers can send
 */
public class OrganizerAlert {
    /**
     * the title of the message
     */
    private String title;
    /**
     * the message that is to be sent
     */
    private String message;
    /**
     * this is the notification channel that we want alerts to be sent through
     */
    private String channelId;
    /**
     * specify if its an event alert to send to attendees or if its a milestone alert for organizers
     */
    private String alertType;

    /**
     * Creates a new OrganizerAlert object
     * @param message the message that will be sent
     */
    public OrganizerAlert(String title, String message, String channelId, String alertType) {
        this.title = title;
        this.message = message;
        this.channelId = channelId;
        this.alertType = alertType;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getAlertType() {
        return alertType;
    }
}
