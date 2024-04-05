package com.example.eventgate.admin;

public class ImageData {
    private String imageType;
    private String associatedId;

    public ImageData(String imageType, String associatedId) {
        this.imageType = imageType;
        this.associatedId = associatedId;
    }

    public String getImageType() {
        return imageType;
    }

    public String getAssociatedId() {
        return associatedId;
    }
}
