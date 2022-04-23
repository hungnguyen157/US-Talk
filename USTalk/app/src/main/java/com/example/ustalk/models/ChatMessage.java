package com.example.ustalk.models;

import android.provider.ContactsContract;

import java.util.Date;

public class ChatMessage {
    public String id, senderID,receicedID,message;
    public Date dateObject,time;
    public boolean sendimage;
    public long feeling;
    public int senderFeeling;
    public int receiverFeeling;
}
