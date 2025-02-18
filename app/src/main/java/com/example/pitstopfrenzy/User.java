package com.example.pitstopfrenzy;

import android.util.Log;

public class User {
    private String userId;
    private String userName;
    private String email;
    private String profileImageUrl;
    private int score;

    // Default constructor (required for Firebase)
    public User() {
    }

    public User(String userId, String userName, String email, String profileImageUrl, int score) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.score = score;
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    //sets a random score for now
    public static int randomScore()
    {
        int randScore = (int) (Math.random() * 100);
        Log.e("XXX", "randScore =  " + randScore);
        return (int) (Math.random() * 100);
    }
}
