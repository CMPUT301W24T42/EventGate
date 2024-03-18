package com.example.eventgate.organizer;

/**
 * This represents an alert that that is sent as a notification to users
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
     * this is the Id of the organizer who created the event
     */
    private String organizerId;
    /**
     * this is the Id of the event who the alert belongs to
     */
    private String eventId;

    /**
     * Creates a new OrganizerAlert object
     * @param title the title of the alert
     * @param message the message of the alert
     * @param channelId the notification channel that the message will be put through
     * @param organizerId the id of the organizer who created the alert
     * @param eventId the id of the event that the alert is for
     */
    public OrganizerAlert(String title, String message, String channelId, String organizerId, String eventId) {
        this.title = title;
        this.message = message;
        this.channelId = channelId;
        this.organizerId = organizerId;
        this.eventId = eventId;
    }

    /**
     * this gets the title of the alert
     * @return a string representing the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * this gets the message of the alert
     * @return a string representing the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * this gets the channel id that the alert will be sent through
     * @return a string representing the channel id
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * this gets the id of the organizer who created the alert
     * @return a string representing the organizer's id
     */
    public String getOrganizerId() {
        return organizerId;
    }

    /**
     * this gets the id of the event that the alert is for
     * @return a string representing the event's id
     */
    public String getEventId() {
        return eventId;
    }
}
