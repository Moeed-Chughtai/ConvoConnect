package com.example.chatapp;

// User class to store all user data
public class User {

    // Private variables for encapsulation
    private String uid, name, phoneNumber, profilePicture;

    // First constructor is empty as Firebase cannot figure out on its own what the constructor does
    // The empty constructor allows Firebase to create a new instance of the object, which it then proceeds to fill in using reflection
    public User() {
    }

    // Second constructor
    public User(String uid, String name, String phoneNumber, String profilePicture) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profilePicture = profilePicture;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
