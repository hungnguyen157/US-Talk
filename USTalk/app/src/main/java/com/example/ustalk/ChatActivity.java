package com.example.ustalk;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vanniktech.emoji.EmojiPopup;

public class ChatActivity extends Activity {
//    ImageView btnEmoji,btnSend;
//    EditText txtMessage;
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.chat_screen);
//        btnEmoji = (ImageView) findViewById(R.id.btnEmoji);
//        btnSend = (ImageView) findViewById(R.id.btnSend);
//        txtMessage = (EditText) findViewById(R.id.txtMessage);
//        EmojiPopup popup = EmojiPopup.Builder.fromRootView(findViewById(R.id.chat_view)).build(txtMessage);
//        btnEmoji.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                popup.toggle();
//            }
//        });
//    }

    ImageView avatar, btn_back, btn_call, btn_video_call, btn_image, btn_micro, btn_emoji, btn_send;
    EditText edit_chat;
    RecyclerView recycler_view_message;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //get widgets
        avatar = (ImageView) findViewById(R.id.avatar);
        btn_back = (ImageView) findViewById(R.id.btn_back);
        btn_call = (ImageView) findViewById(R.id.btn_call);
        btn_video_call = (ImageView) findViewById(R.id.btn_video_call);
        btn_image = (ImageView) findViewById(R.id.btn_image);
        btn_micro = (ImageView) findViewById(R.id.btn_micro);
        btn_emoji = (ImageView) findViewById(R.id.btn_emoji);
        btn_send = (ImageView) findViewById(R.id.btn_send);
        edit_chat = (EditText) findViewById(R.id.edit_chat);
        recycler_view_message = (RecyclerView) findViewById(R.id.recycler_view_message);

        //set listeners
        EmojiPopup popup = EmojiPopup.Builder.fromRootView(findViewById(R.id.chat_view)).build(edit_chat);
        btn_emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.toggle();
            }
        });
    }
}
