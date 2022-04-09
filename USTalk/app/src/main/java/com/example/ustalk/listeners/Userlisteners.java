package com.example.ustalk.listeners;

import com.example.ustalk.models.User;

public interface Userlisteners {
    void onUserClicked(User user);
    void initiateVideoMeeting(User user);
    void initiateAudioMeeting(User user);
}
