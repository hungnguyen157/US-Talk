package com.example.ustalk.models;

import java.util.ArrayList;

public class User {
    public String email, sex, name,imageProfile,id, token;
    public ArrayList<String> Friends;
    public boolean online;
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

    public void setImage(String image){this.imageProfile = image;}
    @Override
    public String toString() {
        return "User{" +
                " email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                '}';
    }
}
