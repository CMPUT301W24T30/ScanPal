package com.example.scanpal.ModelsTest;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import com.example.scanpal.Models.User;


public class UserTest {

    private User user;

    @Before
    public void setUp() {
        user = new User("johnDoe", "John", "Doe", "dummyDeviceToken");
    }

    @Test
    public void constructor_withFourParameters_initializesCorrectly() {
        assertEquals("johnDoe", user.getUsername());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertNotNull(user.getPhoto());
        assertEquals("dummyDeviceToken", user.getDeviceToken());
        assertFalse(user.isAdministrator());
    }

    @Test
    public void constructor_withSixParameters_initializesCorrectly() {
        String photoUrl = "https://example.com/photo.jpg";
        String homepage = "https://example.com";
        user = new User("janeDoe", "Jane", "Doe", photoUrl, homepage, "deviceToken123");

        assertEquals("janeDoe", user.getUsername());
        assertEquals("Jane", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals(photoUrl, user.getPhoto());
        assertEquals(homepage, user.getHomepage());
        assertEquals("deviceToken123", user.getDeviceToken());
    }

    @Test
    public void setUsername_updatesUsernameCorrectly() {
        user.setUsername("newUsername");
        assertEquals("newUsername", user.getUsername());
    }

    @Test
    public void createProfileImage_generatesCorrectUrl() {
        String expectedUrl = "https://www.gravatar.com/avatar/johnDoe?s=400&d=monsterid&r=pg";
        String actualUrl = user.createProfileImage("johnDoe");
        assertEquals(expectedUrl, actualUrl);
    }
}
