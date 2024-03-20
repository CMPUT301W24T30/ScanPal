package com.example.scanpal;

/**
 * Initializes a user that has administrative permissions.
 */
public class Administrator extends User {

    public Administrator(String username, String firstName, String lastName,String deviceToken ) {
        super(username, firstName, lastName,deviceToken);
        this.setAdministrator(true); //set this account to admin
    }
}
