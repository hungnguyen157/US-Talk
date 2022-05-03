package com.example.ustalk.models;

import android.provider.ContactsContract;

import com.google.firebase.firestore.DocumentChange;

import java.util.Date;

public class ChatMessage {
    public String id, senderID,receicedID,message;
    public Date dateObject,time;
    public boolean sendimage;
    public int senderFeeling;
    public int receiverFeeling;

    public static ChatMessage fromDocumentChange(DocumentChange documentChange) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.id = documentChange.getDocument().getId();
        chatMessage.senderID = documentChange.getDocument().getString("senderID");
        chatMessage.receicedID = documentChange.getDocument().getString("RecceiveID");
        chatMessage.message = documentChange.getDocument().getString("Message");
        chatMessage.time = documentChange.getDocument().getDate("Time");
        chatMessage.dateObject = documentChange.getDocument().getDate("Time");
        chatMessage.sendimage = documentChange.getDocument().getBoolean("sendimage");
        try {
            chatMessage.senderFeeling = (int)((long)documentChange.getDocument()
                    .getLong("senderFeeling"));
            chatMessage.receiverFeeling = (int)((long)documentChange.getDocument()
                    .getLong("receiverFeeling"));
        } catch (NullPointerException ex) {
            chatMessage.senderFeeling = -1;
            chatMessage.receiverFeeling = -1;
        }
        return chatMessage;
    }
}
