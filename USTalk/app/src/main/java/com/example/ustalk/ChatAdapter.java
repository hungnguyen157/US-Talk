package com.example.ustalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ustalk.models.ChatMessage;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final ArrayList<ChatMessage> chatMessages;
    private final String senderID;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    public ChatAdapter(ArrayList<ChatMessage> chatMessages,String senderID) {
        this.chatMessages = chatMessages;
        this.senderID = senderID;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == VIEW_TYPE_SENT)
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_text_message_sent,parent,false);
            return new SentMessageViewHolder(view);
        }
        else if (viewType == VIEW_TYPE_RECEIVED)
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_text_message_received,parent,false);
            return new SentMessageViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT)
        {
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        }
        else if (getItemViewType(position) == VIEW_TYPE_RECEIVED)
        {
            {
                ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position));
            }
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }
    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderID.equals(senderID))
        {
            return VIEW_TYPE_SENT;
        }
        else
            return VIEW_TYPE_RECEIVED;
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        TextView sendMes,Time;
        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sendMes = (TextView) itemView.findViewById(R.id.sent_text);
            Time = (TextView) itemView.findViewById(R.id.time_sent);
        }
        public void setData(ChatMessage chatMessage)
        {
            sendMes.setText(chatMessage.message);
            Time.setText(chatMessage.time);
        }
    }
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        TextView sendMes,Time;
        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sendMes = (TextView) itemView.findViewById(R.id.received_text);
            Time = (TextView) itemView.findViewById(R.id.time_received);
        }
        public void setData(ChatMessage chatMessage)
        {
            sendMes.setText(chatMessage.message);
            Time.setText(chatMessage.time);
        }
    }
}
