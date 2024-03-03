package com.example.scanpal;

import java.io.File;

/**
 * Initializes a user with a username, first name, and last name.
 */
public class User {
    private final String username;
    private boolean administrator = false;

    private String firstName;
    private String lastName;

    private String photo; // Profile Photo of the User

    /**
     * Constructs a user with a username, first name, and last name.
     *
     * @param username  The username of the user.
     * @param firstName The first name of the user.
     * @param lastName  The last name of the user.
     */
    public User(String username, String firstName, String lastName) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photo = createProfileImage(username);
    }

    /**
     * Constructs a user with a username, first name, and last name.
     *
     * @param username  The username of the user.
     * @param firstName The first name of the user.
     * @param lastName  The last name of the user.
     * @param photo     The URL of the user's photo.
     */
    public User(String username, String firstName, String lastName, String photo) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photo = photo;
    }

    /**
     * Generates a URL for the default profile image of the user
     * @param username
     * @return
     */
    private String createProfileImage(String username) {
        return "http://www.gravatar.com/avatar/" + username + "?s=55&d=identicon&r=PG";
    }

    public String getUsername() {
        return username;
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
}
