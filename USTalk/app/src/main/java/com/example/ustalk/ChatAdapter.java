package com.example.ustalk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ustalk.databinding.ChatImageMessageReceivedBinding;
import com.example.ustalk.databinding.ChatImageMessageSentBinding;
import com.example.ustalk.databinding.ChatTextMessageReceivedBinding;
import com.example.ustalk.databinding.ChatTextMessageSentBinding;
import com.example.ustalk.databinding.ChatVoiceMessageReceivedBinding;
import com.example.ustalk.databinding.ChatVoiceMessageSentBinding;
import com.example.ustalk.models.ChatMessage;
import com.example.ustalk.utilities.AudioService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final ArrayList<ChatMessage> chatMessages;
    private final String senderID;
    private AudioService audioService;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    public static final int VIEW_TYPE_SENT_IMAGE = 3;
    public static final int VIEW_TYPE_RECEIVED_IMAGE = 4;
    public static final int VIEW_TYPE_SENT_AUDIO = 5;
    public static final int VIEW_TYPE_RECEIVED_AUDIO = 6;

    Context context;
    public ChatAdapter(Context context, ArrayList<ChatMessage> chatMessages, String senderID) {
        this.chatMessages = chatMessages;
        this.senderID = senderID;
        this.context = context;
    }

    //Needed Override functions
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderID.equals(senderID) && !chatMessages.get(position).sendimage)
        {
            return VIEW_TYPE_SENT;
        }
        else if (!chatMessages.get(position).senderID.equals(senderID) && !chatMessages.get(position).sendimage)
        {
            return VIEW_TYPE_RECEIVED;
        }
        else if (chatMessages.get(position).senderID.equals(senderID) && chatMessages.get(position).sendimage)
            return VIEW_TYPE_SENT_IMAGE;
        else
            return VIEW_TYPE_RECEIVED_IMAGE;
    }

    //Other needed functions
    private String getReadableDateTime(Date date)
    {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date);
    }

    private void viewImageFullSize(byte[] bytes_bitmap){
        Intent intent = new Intent(context, ViewImageFullSizeActivity.class);
        intent.putExtra("bytes_bitmap", bytes_bitmap);
        context.startActivity(intent);
    }

    private void setPlayOrPauseAudio(ImageButton imgBtn, SeekBar seekBar, Chronometer chronometer,
                                     Runnable updater, Handler handler){
        if (audioService != null){
            if (audioService.isPlaying()){
                handler.removeCallbacks(updater);
                chronometer.stop();
                audioService.pauseAudio();
                imgBtn.setImageResource(R.drawable.ic_round_play_arrow_24);
            }
            else{
                String url = "";
                audioService.playAudioFromURL(url, new AudioService.OnPlayCallBack() {
                    @Override
                    public void onFinished() {
                        seekBar.setProgress(0);
                        chronometer.setBase(audioService.getDuration());
                        imgBtn.setImageResource(R.drawable.ic_round_play_arrow_24);
                    }
                });
                imgBtn.setImageResource(R.drawable.ic_round_pause_24);
                chronometer.setBase(audioService.getDuration() - audioService.getCurrentPostion());
                chronometer.start();
                updateSeekerBar(updater, seekBar, handler);
            }
        }
    }

//    private String milliSecondsToTimer(long milliSeconds){
//        String timeString = "";
//        String secondsString;
//
//        final int hoursToMilliSeconds = 1000 * 60 * 60;
//        int hours = (int) (milliSeconds / hoursToMilliSeconds);
//        int minutes = (int) ((milliSeconds % hoursToMilliSeconds) / (1000 * 60));
//        int seconds = (int) (((milliSeconds % hoursToMilliSeconds) % (1000 * 60)) / 1000);
//
//        if (hours > 0){
//            if (hours < 10) timeString = "0" + hours + ":";
//            else timeString = hours + ":";
//        }
//        if (minutes < 10) timeString += "0";
//        if (seconds < 10) secondsString = "0" + seconds;
//        else secondsString = "" + seconds;
//
//        timeString = timeString + minutes + ":" + secondsString;
//        return timeString;
//    }

    private void updateSeekerBar(Runnable updater, SeekBar seekBar, Handler handler){
        if (audioService.isPlaying()){
            seekBar.setProgress(
                    (int)(((float) audioService.getCurrentPostion() / audioService.getDuration()) * 100));
            handler.postDelayed(updater, 1000);
        }
    }

    //Needed ViewHolder classes
    private class SentMessageViewHolder extends RecyclerView.ViewHolder{
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

    private class SentVoiceViewHolder extends RecyclerView.ViewHolder{
        public ChatVoiceMessageSentBinding binding;
        private Runnable updater = null;
        private Handler handler = null;
        public SentVoiceViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        @SuppressLint("ClickableViewAccessibility")
        public void setData(ChatMessage chatMessage){
            audioService = new AudioService(context);
            handler = new Handler();
            updater = new Runnable() {
                @Override
                public void run() {
                    updateSeekerBar(updater, binding.soundSeekbar, handler);
                }
            };
            binding.soundSeekbar.setMax(100);
            binding.soundSeekbar.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    SeekBar seekBar = (SeekBar) view;
                    int playPosition = (int) ((audioService.getDuration() / 100) * seekBar.getProgress());
                    audioService.seekTo(playPosition);
                    binding.timeLast.setBase(audioService.getDuration() - audioService.getCurrentPostion());
                    return false;
                }
            });
            binding.btnPlayOrStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setPlayOrPauseAudio(binding.btnPlayOrStop, binding.soundSeekbar, binding.timeLast,
                            updater, handler);
                }
            });
        }
    }

    private class ReceivedVoiceViewHolder extends RecyclerView.ViewHolder{
        public ChatVoiceMessageReceivedBinding binding;
        private Runnable updater = null;
        private Handler handler = null;
        public ReceivedVoiceViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        @SuppressLint("ClickableViewAccessibility")
        public void setData(ChatMessage chatMessage){
            audioService = new AudioService(context);
            handler = new Handler();
            updater = new Runnable() {
                @Override
                public void run() {
                    updateSeekerBar(updater, binding.soundSeekbar, handler);
                }
            };
            binding.soundSeekbar.setMax(100);
            binding.soundSeekbar.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    SeekBar seekBar = (SeekBar) view;
                    int playPosition = (int) ((audioService.getDuration() / 100) * seekBar.getProgress());
                    audioService.seekTo(playPosition);
                    binding.timeLast.setBase(audioService.getDuration() - audioService.getCurrentPostion());
                    return false;
                }
            });
            binding.btnPlayOrStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setPlayOrPauseAudio(binding.btnPlayOrStop, binding.soundSeekbar, binding.timeLast,
                            updater, handler);
                }
            });
        }
    }
}
