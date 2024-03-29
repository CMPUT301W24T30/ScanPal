package com.example.scanpal.Models;

import java.io.Serializable;


/**
 * Initializes a user with a username, first name, and last name.
 * Implements Serializable for object serialization and deserialization.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private boolean administrator = false;
    private String firstName;
    private String lastName;
    private String photo; // Profile Photo of the User
    private String deviceToken;

    /**
     * Constructs a user with a username, first name, and last name.
     *
     * @param username  The username of the user.
     * @param firstName The first name of the user.
     * @param lastName  The last name of the user.
     */
    public User(String username, String firstName, String lastName, String deviceToken) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photo = createProfileImage(username);
        this.deviceToken = deviceToken;

    }

    /**
     * Constructs a user with a username, first name, and last name.
     *
     * @param username  The username of the user.
     * @param firstName The first name of the user.
     * @param lastName  The last name of the user.
     * @param photo     The URL of the user's photo.
     */
    public User(String username, String firstName, String lastName, String photo, String deviceToken) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photo = photo;
        this.deviceToken = deviceToken;
    }

    public User() {

    }

    /**
     * Generates a URL for a default profile image based on the username. This method
     * utilizes Gravatar's service to create a URL pointing to an identicon image,
     * which serves as the user's default profile picture. The resulting image has
     * a size of 100 pixels and is suitable for all audiences (rated PG).
     *
     * @param username The username for which the profile image URL is generated.
     * @return A string representing the URL to the generated default profile image.
     */
    public String createProfileImage(String username) {
        return "https://www.gravatar.com/avatar/" + username + "?s=400&d=wavatar&r=x";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isAdministrator() {
        return administrator;
    }

    public void setAdministrator(boolean administrator) {
        this.administrator = administrator;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
