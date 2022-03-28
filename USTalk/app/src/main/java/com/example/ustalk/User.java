package com.example.ustalk;

public class User {
    public String uid, email, name, sex;

public class User {
    public String email, sex, name,uid;
    public ArrayList<String> Friends;
    public User() {
    }

    public User(String name, String email, String sex) {
        this.email = email;
        this.name = name;
        this.sex = sex;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
                "uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                '}';
    }
}
