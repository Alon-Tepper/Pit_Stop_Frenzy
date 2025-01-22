package com.example.pitstopfrenzy;

public class User {
    private String userId;
    private String userName;
    private String email;
    private String profileImageUrl;

    // Default constructor (required for Firebase)
    public User() {
    }

    public User(String userId, String userName, String email, String profileImageUrl) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
