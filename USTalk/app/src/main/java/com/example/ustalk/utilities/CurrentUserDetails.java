package com.example.ustalk.utilities;

import android.util.Log;

import com.example.ustalk.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

public class CurrentUserDetails {

    public static CurrentUserDetails instance;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    User user;
    private String uid;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void updateUser(User user) {
        this.user = user;
        db.collection("users").document(uid).set(user).addOnFailureListener(e -> Log.e("updateUser", e.getMessage()));
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
