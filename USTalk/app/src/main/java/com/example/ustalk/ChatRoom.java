package com.example.ustalk;

import com.example.ustalk.models.ChatMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatRoom {
    public List<String> memberIds;
    public ArrayList<ChatMessage> messages = new ArrayList<>();

    public ChatRoom() {

    }

    public ChatRoom(String[] ids) {
        memberIds = Arrays.asList(ids);
    }
}
