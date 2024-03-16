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
     * this is the Id of the organizer who created the event
     */
    private String organizerId;
    /**
     * this is the Id of the event who the alert belongs to
     */
    private String eventId;

    /**
     * Creates a new OrganizerAlert object
     * @param message the message that will be sent
     */
    public OrganizerAlert(String title, String message, String channelId, String organizerId, String eventId) {
        this.title = title;
        this.message = message;
        this.channelId = channelId;
        this.organizerId = organizerId;
        this.eventId = eventId;
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

    public String getOrganizerId() {
        return organizerId;
    }

    public String getEventId() {
        return eventId;
    }
}
