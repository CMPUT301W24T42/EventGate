package com.example.eventgate;

import static org.junit.Assert.assertEquals;

import com.example.eventgate.admin.ImageData;

import org.junit.Test;

public class ImageDataTest {
    /**
     * tests the constructor of ImageData
     */
    @Test
    public void testImageTypeConstructor() {
       ImageData imageData = new ImageData("poster", "randomEventId");
       assertEquals("poster", imageData.getImageType());
       assertEquals("randomEventId", imageData.getAssociatedId());
    }
}
