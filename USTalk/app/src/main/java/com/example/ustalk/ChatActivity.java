package com.example.ustalk;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.vanniktech.emoji.EmojiPopup;

public class ChatActivity extends Activity {
    ImageView btnEmoji,btnSend;
    EditText txtMessage;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_screen);
        btnEmoji = (ImageView) findViewById(R.id.btnEmoji);
        btnSend = (ImageView) findViewById(R.id.btnSend);
        txtMessage = (EditText) findViewById(R.id.txtMessage);
        EmojiPopup popup = EmojiPopup.Builder.fromRootView(findViewById(R.id.chat_view)).build(txtMessage);
        btnEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.toggle();
            }
        });
    }
}
