package com.example.scanpal;

public abstract class User {
    private String userName;
    protected boolean adminFlag;//so child classes can access
    private int permissions; // 'permissions' level, either 0, 1 or 2?

    //add field for user profile data here

    User(String username) {
        this.userName = username;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isAdminFlag() {
        return adminFlag;
    }

    public int getPermissions() {
        return permissions;
    }

    public void setUserName(String newName) {
        this.userName = newName;

        // do stuff to update name in the database
    }

    public void setPermissions(int newPermissions) {
        this.permissions = newPermissions;

        // do stuff to update perms in the database
    }
}
