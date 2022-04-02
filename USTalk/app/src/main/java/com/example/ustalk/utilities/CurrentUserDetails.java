package com.example.ustalk.utilities;

import com.example.ustalk.models.User;

public class CurrentUserDetails {

    public static CurrentUserDetails instance;
    User user;
    private String uid;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static CurrentUserDetails getInstance() {
        if (instance == null) instance = new CurrentUserDetails();
        return instance;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }
}
