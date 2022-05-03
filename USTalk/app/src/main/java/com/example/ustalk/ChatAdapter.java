package com.example.ustalk;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.example.ustalk.utilities.AudioPlayerService;
import com.google.firebase.firestore.FirebaseFirestore;

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
    public static final int VIEW_TYPE_SENT_IMAGE = 3;
    public static final int VIEW_TYPE_RECEIVED_IMAGE = 4;
    public static final int VIEW_TYPE_SENT_VOICE = 5;
    public static final int VIEW_TYPE_RECEIVED_VOICE = 6;

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
        if(viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(ChatTextMessageSentBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
        else if (viewType == VIEW_TYPE_RECEIVED) {
            return new ReceivedMessageViewHolder(ChatTextMessageReceivedBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
        else if (viewType ==VIEW_TYPE_RECEIVED_IMAGE ) {
            return new ReceivedImageViewHolder(ChatImageMessageReceivedBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
        else if (viewType == VIEW_TYPE_SENT_IMAGE) {
            return new SentImageViewHolder(ChatImageMessageSentBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
        else if (viewType == VIEW_TYPE_SENT_VOICE) {
            return new SentVoiceViewHolder(ChatVoiceMessageSentBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
        return new ReceivedVoiceViewHolder(ChatVoiceMessageReceivedBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        }
        else if (getItemViewType(position) == VIEW_TYPE_RECEIVED) {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position));
        }
        else if (getItemViewType(position) == VIEW_TYPE_RECEIVED_IMAGE) {
            ((ReceivedImageViewHolder) holder).setData(chatMessages.get(position));
        }
        else if(getItemViewType(position) == VIEW_TYPE_SENT_IMAGE) {
            ((SentImageViewHolder) holder).setData(chatMessages.get(position));
        }
        else if (getItemViewType(position) == VIEW_TYPE_SENT_VOICE) {
            ((SentVoiceViewHolder) holder).setData(chatMessages.get(position));
        }
        else if (getItemViewType(position) == VIEW_TYPE_RECEIVED_VOICE) {
            ((ReceivedVoiceViewHolder) holder).setData(chatMessages.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderID.equals(senderID)
                && !chatMessages.get(position).sendimage && !chatMessages.get(position).sendvoice) {
            return VIEW_TYPE_SENT;
        }
        else if (!chatMessages.get(position).senderID.equals(senderID)
                && !chatMessages.get(position).sendimage && !chatMessages.get(position).sendvoice) {
            return VIEW_TYPE_RECEIVED;
        }
        else if (chatMessages.get(position).senderID.equals(senderID)
                && chatMessages.get(position).sendimage) {
            return VIEW_TYPE_SENT_IMAGE;
        }
        else if (!chatMessages.get(position).senderID.equals(senderID)
                && chatMessages.get(position).sendimage) {
            return VIEW_TYPE_RECEIVED_IMAGE;
        }
        else if (chatMessages.get(position).senderID.equals(senderID)
                && chatMessages.get(position).sendvoice) {
            return VIEW_TYPE_SENT_VOICE;
        }
        else return VIEW_TYPE_RECEIVED_VOICE;
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
                                     AudioPlayerService audioPlayerService, Runnable updater, Handler handler){
        if (audioPlayerService != null){
            if (audioPlayerService.isPlaying()){
                handler.removeCallbacks(updater);
                chronometer.stop();
                audioPlayerService.pauseAudio();
                imgBtn.setImageResource(R.drawable.ic_round_play_arrow_24);
            }
            else{
                audioPlayerService.playAudio(() -> {
                    seekBar.setProgress(100);
                    chronometer.stop();
                    chronometer.setBase(SystemClock.elapsedRealtime() + audioPlayerService.getDuration());
                    imgBtn.setImageResource(R.drawable.ic_round_play_arrow_24);
                    handler.removeCallbacks(updater);
                });
                imgBtn.setImageResource(R.drawable.ic_round_pause_24);
                chronometer.setBase(SystemClock.elapsedRealtime() +
                        (audioPlayerService.getDuration() - audioPlayerService.getCurrentPosition()));
                chronometer.start();
                updateSeekerBar(audioPlayerService, updater, seekBar, handler);
            }
        }
    }
    private void updateSeekerBar(AudioPlayerService audioPlayerService,
                                 Runnable updater, SeekBar seekBar, Handler handler){
        if (audioPlayerService.isPlaying()){
            seekBar.setProgress(
                    (int)(((float) audioPlayerService.getCurrentPosition() / audioPlayerService.getDuration()) * 100));
            handler.postDelayed(updater, 1000);
        }
    }

    private void showReactPicker(ChatMessage chatMessage){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.react_picker_layout);
        dialog.setCanceledOnTouchOutside(true);

        final boolean isSender = chatMessage.senderID.equals(senderID);

        dialog.findViewById(R.id.btnLike).setOnClickListener(view ->
                chooseReactEmotion(chatMessage, R.drawable.react_like, dialog, isSender));
        dialog.findViewById(R.id.btnLove).setOnClickListener(view ->
                chooseReactEmotion(chatMessage, R.drawable.react_heart, dialog, isSender));
        dialog.findViewById(R.id.btnHaha).setOnClickListener(view ->
                chooseReactEmotion(chatMessage, R.drawable.react_haha, dialog, isSender));
        dialog.findViewById(R.id.btnWow).setOnClickListener(view ->
                chooseReactEmotion(chatMessage, R.drawable.react_wow, dialog, isSender));
        dialog.findViewById(R.id.btnSad).setOnClickListener(view ->
                chooseReactEmotion(chatMessage, R.drawable.react_sad, dialog, isSender));
        dialog.findViewById(R.id.btnAngry).setOnClickListener(view ->
                chooseReactEmotion(chatMessage, R.drawable.react_angry, dialog, isSender));

        if (isSender) {
            setChosenReactBackground(dialog, chatMessage.senderFeeling);
        }
        else setChosenReactBackground(dialog, chatMessage.receiverFeeling);

        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    private void setChosenReactBackground(Dialog dialog, final int feeling) {
        ImageView btnChosenReact = null;
        switch (feeling) {
            case (R.drawable.react_like): {
                btnChosenReact = dialog.findViewById(R.id.btnLike);
                break;
            }
            case (R.drawable.react_heart): {
                btnChosenReact = dialog.findViewById(R.id.btnLove);
                break;
            }
            case (R.drawable.react_haha): {
                btnChosenReact = dialog.findViewById(R.id.btnHaha);
                break;
            }
            case (R.drawable.react_wow): {
                btnChosenReact = dialog.findViewById(R.id.btnWow);
                break;
            }
            case (R.drawable.react_sad): {
                btnChosenReact = dialog.findViewById(R.id.btnSad);
                break;
            }
            case (R.drawable.react_angry): {
                btnChosenReact = dialog.findViewById(R.id.btnAngry);
                break;
            }
        }
        if (btnChosenReact != null) {
            btnChosenReact.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.light_gray)));
        }
    }

    private void chooseReactEmotion(ChatMessage chatMessage, int reactId, Dialog dialog, boolean isSender){
        final FirebaseFirestore database = FirebaseFirestore.getInstance();
        if (isSender) {
            if (chatMessage.senderFeeling != reactId) {
                chatMessage.senderFeeling = reactId;
            }
            else {
                chatMessage.senderFeeling = -1;
            }
            database.collection("chat").document(chatMessage.id)
                    .update("senderFeeling", chatMessage.senderFeeling);
        }
        else {
            if (chatMessage.receiverFeeling != reactId) {
                chatMessage.receiverFeeling = reactId;
            }
            else {
                chatMessage.receiverFeeling = -1;
            }
            database.collection("chat").document(chatMessage.id)
                    .update("receiverFeeling", chatMessage.receiverFeeling);
        }

        dialog.dismiss();
    }

    private void setReactImage(ImageView imgSenderReact, ImageView imgReceiverReact,
                               int senderFeeling, int receiverFeeling) {
        if (senderFeeling == -1) {
            imgSenderReact.setImageResource(R.drawable.ic_baseline_insert_emoticon);
        }
        else {
            imgSenderReact.setImageResource(senderFeeling);
        }
        imgSenderReact.requestLayout();

        if (receiverFeeling == -1) {
            imgReceiverReact.setImageResource(R.drawable.ic_baseline_insert_emoticon);
        }
        else {
            imgReceiverReact.setImageResource(receiverFeeling);
        }
        imgReceiverReact.requestLayout();
    }

    //Needed ViewHolder classes
    private class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ChatTextMessageSentBinding binding;
        public SentMessageViewHolder(ChatTextMessageSentBinding chatTextMessageSentBinding) {
            super(chatTextMessageSentBinding.getRoot());
            binding = chatTextMessageSentBinding;
        }
        public void setData(ChatMessage chatMessage)
        {
            binding.sentText.setText(chatMessage.message);
            binding.timeSent.setText(getReadableDateTime(chatMessage.time));
            setReactImage(binding.imgSenderReact, binding.imgReceiverReact,
                    chatMessage.senderFeeling, chatMessage.receiverFeeling);

            binding.sentText.setOnLongClickListener(view -> {
                showReactPicker(chatMessage);
                return false;
            });
        }
    }

    private class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ChatTextMessageReceivedBinding binding;
        public ReceivedMessageViewHolder(ChatTextMessageReceivedBinding chatTextMessageReceivedBinding) {
            super(chatTextMessageReceivedBinding.getRoot());
            binding = chatTextMessageReceivedBinding;
        }
        public void setData(ChatMessage chatMessage)
        {
            binding.receivedText.setText(chatMessage.message);
            binding.timeReceived.setText(getReadableDateTime(chatMessage.time));
            setReactImage(binding.imgSenderReact, binding.imgReceiverReact,
                    chatMessage.senderFeeling, chatMessage.receiverFeeling);

            binding.receivedText.setOnLongClickListener(view -> {
                showReactPicker(chatMessage);
                return false;
            });
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
            setReactImage(binding.imgSenderReact, binding.imgReceiverReact,
                    chatMessage.senderFeeling, chatMessage.receiverFeeling);

            binding.sentImage.setOnClickListener(view -> viewImageFullSize(bytes));
            binding.sentImage.setOnLongClickListener(view -> {
                showReactPicker(chatMessage);
                return false;
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
            setReactImage(binding.imgSenderReact, binding.imgReceiverReact,
                    chatMessage.senderFeeling, chatMessage.receiverFeeling);

            binding.receivedImage.setOnClickListener(view -> viewImageFullSize(bytes));
            binding.receivedImage.setOnLongClickListener(view -> {
                showReactPicker(chatMessage);
                return false;
            });
        }
    }

    private class SentVoiceViewHolder extends RecyclerView.ViewHolder{
        public ChatVoiceMessageSentBinding binding;
        AudioPlayerService audioPlayerService;
        private Runnable updater = null;
        private Handler handler = null;
        public SentVoiceViewHolder(@NonNull ChatVoiceMessageSentBinding chatVoiceMessageSentBinding) {
            super(chatVoiceMessageSentBinding.getRoot());
            binding = chatVoiceMessageSentBinding;
        }
        @SuppressLint("ClickableViewAccessibility")
        public void setData(ChatMessage chatMessage){
            audioPlayerService = new AudioPlayerService(chatMessage.message);
            handler = new Handler();
            updater = () -> updateSeekerBar(audioPlayerService, updater, binding.soundSeekbar, handler);
            binding.soundSeekbar.setMax(100);
            binding.soundSeekbar.setProgress(100);
            setReactImage(binding.imgSenderReact, binding.imgReceiverReact,
                    chatMessage.senderFeeling, chatMessage.receiverFeeling);
            binding.timeLast.setBase(SystemClock.elapsedRealtime() + audioPlayerService.getDuration());

            binding.soundSeekbar.setOnTouchListener((view, motionEvent) -> {
                SeekBar seekBar = (SeekBar) view;
                int playPosition = (int) ((audioPlayerService.getDuration() / 100) * seekBar.getProgress());
                audioPlayerService.seekTo(playPosition);
                binding.timeLast.setBase(SystemClock.elapsedRealtime() +
                        (audioPlayerService.getDuration() - audioPlayerService.getCurrentPosition()));
                return false;
            });
            binding.btnPlayOrStop.setOnClickListener(view ->
                    setPlayOrPauseAudio(binding.btnPlayOrStop, binding.soundSeekbar, binding.timeLast,
                            audioPlayerService, updater, handler));
            binding.voiceLayout.setOnLongClickListener(view -> {
                showReactPicker(chatMessage);
                return false;
            });
        }
    }

    private class ReceivedVoiceViewHolder extends RecyclerView.ViewHolder{
        public ChatVoiceMessageReceivedBinding binding;
        AudioPlayerService audioPlayerService;
        private Runnable updater = null;
        private Handler handler = null;
        public ReceivedVoiceViewHolder(@NonNull ChatVoiceMessageReceivedBinding chatVoiceMessageReceivedBinding) {
            super(chatVoiceMessageReceivedBinding.getRoot());
            binding = chatVoiceMessageReceivedBinding;
        }
        @SuppressLint("ClickableViewAccessibility")
        public void setData(ChatMessage chatMessage){
            audioPlayerService = new AudioPlayerService(chatMessage.message);
            handler = new Handler();
            updater = () -> updateSeekerBar(audioPlayerService, updater, binding.soundSeekbar, handler);
            binding.soundSeekbar.setMax(100);
            binding.soundSeekbar.setProgress(100);
            setReactImage(binding.imgSenderReact, binding.imgReceiverReact,
                    chatMessage.senderFeeling, chatMessage.receiverFeeling);
            binding.timeLast.setBase(SystemClock.elapsedRealtime() + audioPlayerService.getDuration());

            binding.soundSeekbar.setOnTouchListener((view, motionEvent) -> {
                SeekBar seekBar = (SeekBar) view;
                int playPosition = (int) ((audioPlayerService.getDuration() / 100) * seekBar.getProgress());
                audioPlayerService.seekTo(playPosition);
                binding.timeLast.setBase(SystemClock.elapsedRealtime() +
                        (audioPlayerService.getDuration() - audioPlayerService.getCurrentPosition()));
                return false;
            });
            binding.btnPlayOrStop.setOnClickListener(view ->
                    setPlayOrPauseAudio(binding.btnPlayOrStop, binding.soundSeekbar, binding.timeLast,
                            audioPlayerService, updater, handler));
            binding.voiceLayout.setOnLongClickListener(view -> {
                showReactPicker(chatMessage);
                return false;
            });
        }
    }
}
