package com.se1.dao;

/**
 * Created by Abhitej on 2/19/2015.
 */
public class User {
    private String emailId;
    private String password;

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

    private String firstName;
    private String lastName;

    public int getDropBoxAdded() {
        return dropBoxAdded;
    }

    public void setDropBoxAdded(int dropBoxAdded) {
        this.dropBoxAdded = dropBoxAdded;
    }

    private int dropBoxAdded;

    public int getLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(int loggedIn) {
        this.loggedIn = loggedIn;
    }

    private int loggedIn;

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return password;
    }
}