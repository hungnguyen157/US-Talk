package com.example.ustalk.models;

import java.util.ArrayList;

public class User {
    public String email, sex, name;
    public ArrayList<String> Friends;

    public User() {
    }

    public User(String name, String email, String sex) {
        this.email = email;
        this.name = name;
        this.sex = sex;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    @Override
    public String toString() {
        return "User{" +
                " email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                '}';
    }
}