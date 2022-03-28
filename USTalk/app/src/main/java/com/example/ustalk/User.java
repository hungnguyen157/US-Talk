package com.example.ustalk;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class User {
    public String email, sex, name,uid;
    public ArrayList<String> Friends;
    public User() {
    }

    public User(String name, String sex, String email) {
        this.email = email;
        this.name = name;
        this.sex = sex;
    }
}
