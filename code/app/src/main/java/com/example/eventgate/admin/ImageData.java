package com.example.eventgate.admin;

/**
 * this object holds data about an image
 */
public class ImageData {
    /**
     * the type of image (either event poster or profile picture)
     */
    private String imageType;
    /**
     * the id associated with the image (either eventId for posters or attendeeId for profile pictures)
     */
    private String associatedId;

    /**
     * constructs a new ImageData object
     * @param imageType the type of image
     * @param associatedId the id associated with the image
     */
    public ImageData(String imageType, String associatedId) {
        this.imageType = imageType;
        this.associatedId = associatedId;
    }

    /**
     * gets the image type
     * @return the type of the image
     */
    public String getImageType() {
        return imageType;
    }

    /**
     * gets the associated id
     * @return the associated id
     */
    public String getAssociatedId() {
        return associatedId;
    }
}
