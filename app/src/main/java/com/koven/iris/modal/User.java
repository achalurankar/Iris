package com.koven.iris.modal;

public class User {

    private String username, userId, picUrl;

    public User(){
        //empty constructor
    }

    public User(String username, String userId, String picUrl) {
        this.username = username;
        this.userId = userId;
        this.picUrl = picUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }
}
