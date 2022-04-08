package com.example.ustalk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.renderscript.ScriptGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ustalk.databinding.ChatImageMessageReceivedBinding;
import com.example.ustalk.databinding.ChatImageMessageSentBinding;
import com.example.ustalk.databinding.ChatTextMessageReceivedBinding;
import com.example.ustalk.databinding.ChatTextMessageSentBinding;
import com.example.ustalk.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final ArrayList<ChatMessage> chatMessages;
    private final String senderID;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    public static final int VIEW_TYPE_SENT_IMAGE =3;
    public static final int VIEW_TYPE_RECEIVED_IMAGE = 4;

    Context context;
    public ChatAdapter(Context context, ArrayList<ChatMessage> chatMessages, String senderID) {
        this.chatMessages = chatMessages;
        this.senderID = senderID;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == VIEW_TYPE_SENT)
        {
            return new SentMessageViewHolder(ChatTextMessageSentBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
        else if (viewType == VIEW_TYPE_RECEIVED)
        {
            return new ReceivedMessageViewHolder(ChatTextMessageReceivedBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
        else if (viewType ==VIEW_TYPE_RECEIVED_IMAGE )
        {
            return new ReceivedImageViewHolder(ChatImageMessageReceivedBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
        else if (viewType == VIEW_TYPE_SENT_IMAGE)
        {
            return new SentImageViewHolder(ChatImageMessageSentBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
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
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position));
        }
        else if (getItemViewType(position) == VIEW_TYPE_RECEIVED_IMAGE)
        {
            ((ReceivedImageViewHolder) holder).setData(chatMessages.get(position));
        }
        else if(getItemViewType(position) == VIEW_TYPE_SENT_IMAGE)
        {
            ((SentImageViewHolder) holder).setData(chatMessages.get(position));
        }
    }
    private String getReadableDateTime(Date date)
    {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date);
    }
    @Override
    public int getItemCount() {
        return chatMessages.size();
    }
    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderID.equals(senderID) && chatMessages.get(position).sendimage == false)
        {
            return VIEW_TYPE_SENT;
        }
        else if (chatMessages.get(position).senderID.equals(senderID)== false && chatMessages.get(position).sendimage == false)
        {
            return VIEW_TYPE_RECEIVED;
        }
        else if (chatMessages.get(position).senderID.equals(senderID) && chatMessages.get(position).sendimage == true)
            return VIEW_TYPE_SENT_IMAGE;
        else
            return VIEW_TYPE_RECEIVED_IMAGE;
    }

    private class SentMessageViewHolder extends RecyclerView.ViewHolder{
        TextView sendMes,Time;
        private final ChatTextMessageSentBinding biding;
        public SentMessageViewHolder(ChatTextMessageSentBinding chatTextMessageSentBinding) {
            super(chatTextMessageSentBinding.getRoot());
            biding = chatTextMessageSentBinding;
        }
        public void setData(ChatMessage chatMessage)
        {
            biding.sentText.setText(chatMessage.message);
            biding.timeSent.setText(getReadableDateTime(chatMessage.time));
        }
    }
    private class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ChatTextMessageReceivedBinding biding;
        public ReceivedMessageViewHolder(ChatTextMessageReceivedBinding chatTextMessageReceivedBinding) {
            super(chatTextMessageReceivedBinding.getRoot());
            biding = chatTextMessageReceivedBinding;
        }
        public void setData(ChatMessage chatMessage)
        {
            biding.receivedText.setText(chatMessage.message);
            biding.timeReceived.setText(getReadableDateTime(chatMessage.time));
        }
    }

    private class SentImageViewHolder extends RecyclerView.ViewHolder{
        public ChatImageMessageSentBinding binding;
        public SentImageViewHolder(ChatImageMessageSentBinding chatImageMessageSentBinding) {
            super(chatImageMessageSentBinding.getRoot());
            binding = chatImageMessageSentBinding;
        }
        public void setData(ChatMessage chatMessage)
        {
            byte[] bytes = Base64.getDecoder().decode(chatMessage.message);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            binding.sentImage.setImageBitmap(bitmap);
            binding.timeSent.setText(getReadableDateTime(chatMessage.time));

            binding.sentImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewImageFullSize(bytes);
                }
            });
        }
    }

    private class ReceivedImageViewHolder extends RecyclerView.ViewHolder{
        public ChatImageMessageReceivedBinding binding;
        public ReceivedImageViewHolder(ChatImageMessageReceivedBinding chatImageMessageReceivedBinding) {
            super(chatImageMessageReceivedBinding.getRoot());
            binding = chatImageMessageReceivedBinding;
        }
        public void setData(ChatMessage chatMessage)
        {
            byte[] bytes = Base64.getDecoder().decode(chatMessage.message);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            binding.receivedImage.setImageBitmap(bitmap);
            binding.timeReceived.setText(getReadableDateTime(chatMessage.time));

            binding.receivedImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewImageFullSize(bytes);
                }
            });
        }
    }

    private void viewImageFullSize(byte[] bytes_bitmap){
        Intent intent = new Intent(context, ViewImageFullSizeActivity.class);
        intent.putExtra("bytes_bitmap", bytes_bitmap);
        context.startActivity(intent);
    }
}
