package com.example.scanpal.ModelsTest;

import com.example.scanpal.Models.ImageData;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ImageDataTest {

    private ImageData imageData;

    @Before
    public void setUp() {
        imageData = new ImageData("http://example.com/image.jpg", "Title", "Description", "/images", "image.jpg");
    }

    @Test
    public void constructor_initializesPropertiesCorrectly() {
        assertEquals("http://example.com/image.jpg", imageData.getImageURL());
        assertEquals("Title", imageData.getTitle());
        assertEquals("Description", imageData.getDescription());
        assertEquals("/images", imageData.getFolderPath());
        assertEquals("image.jpg", imageData.getFileName());
    }

    @Test
    public void getImage_returnsCorrectImageURL() {
        assertEquals("http://example.com/image.jpg", imageData.getImage());
    }

    @Test
    public void getters_returnCorrectValues() {
        // Title
        assertEquals("The title should be 'Title'", "Title", imageData.getTitle());

        // Description
        assertEquals("The description should be 'Description'", "Description", imageData.getDescription());

        // Folder Path
        assertEquals("The folder path should be '/images'", "/images", imageData.getFolderPath());

        // File Name
        assertEquals("The file name should be 'image.jpg'", "image.jpg", imageData.getFileName());
    }
}
