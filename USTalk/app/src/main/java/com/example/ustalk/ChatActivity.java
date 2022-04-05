package com.example.ustalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ustalk.models.ChatMessage;
import com.example.ustalk.models.User;
import com.example.ustalk.utilities.PreferenceManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.vanniktech.emoji.EmojiPopup;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.SimpleFormatter;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener,EventListener {
    private ArrayList<ChatMessage> Message;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    ImageView avatar, btn_back, btn_call, btn_video_call, btn_image, btn_micro, btn_emoji, btn_send;
    TextView name;
    EditText edit_chat;
    RecyclerView recycler_view_message;
    LinearLayout contact_info;
    String receiveID;
    String receiveimage;
    String receivename;
    EmojiPopup popup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //get widgets
        name = (TextView) findViewById(R.id.name);
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
        contact_info = (LinearLayout) findViewById(R.id.contact_info);
        btn_back.setOnClickListener(this);

        //set OnclickListener for buttons
        btn_back.setOnClickListener(this);
        btn_call.setOnClickListener(this);
        btn_video_call.setOnClickListener(this);
        btn_image.setOnClickListener(this);
        btn_micro.setOnClickListener(this);
        btn_emoji.setOnClickListener(this);
        btn_send.setOnClickListener(this);
        contact_info.setOnClickListener(this);

        //set Userlisteners
        popup = EmojiPopup.Builder.fromRootView(findViewById(R.id.chat_view)).build(edit_chat);
//        btn_emoji.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                popup.toggle();
//            }
//        });
        //btn_send.setOnClickListener(v->SendMes());

        loadReceiverDetails();
        init();
        ListenMes();
    }
    private void init()
    {
        preferenceManager = new PreferenceManager(getApplicationContext());
        Message = new ArrayList<>();
        chatAdapter = new ChatAdapter(this,Message,preferenceManager.getString("UID"));
        recycler_view_message.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }
    private void loadReceiverDetails()
    {
        receiveID = getIntent().getStringExtra("receiveID");
        receivename = getIntent().getStringExtra("name");
        receiveimage=getIntent().getStringExtra("image");
        name.setText(receivename);
        Glide.with(ChatActivity.this).load(receiveimage).into(avatar);
    }
    public void SendMes()
    {
        HashMap<String,Object> mes = new HashMap<>();
        mes.put("senderID",preferenceManager.getString("UID"));
        mes.put("RecceiveID",receiveID);
        mes.put("Message",edit_chat.getText().toString());
        mes.put("Time",new Date());
        database.collection("chat").add(mes);
        edit_chat.setText(null);
        System.out.println(preferenceManager.getString("UID"));
    }
    private void ListenMes()
    {
        database.collection("chat")
                .whereEqualTo("senderID",preferenceManager.getString("UID"))
                .whereEqualTo("RecceiveID",receiveID)
                .addSnapshotListener(eventListener);
        database.collection("chat")
                .whereEqualTo("senderID",receiveID)
                .whereEqualTo("RecceiveID",preferenceManager.getString("UID"))
                .addSnapshotListener(eventListener);
    }
    private final com.google.firebase.firestore.EventListener<QuerySnapshot> eventListener =(value, error) ->{
        if(error != null)
        {
            return ;
        }
        if(value != null)
        {
            int count = Message.size();
            for (DocumentChange documentChange : value.getDocumentChanges())
            {
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderID = documentChange.getDocument().getString("senderID");
                    chatMessage.receicedID = documentChange.getDocument().getString("RecceiveID");
                    chatMessage.message = documentChange.getDocument().getString("Message");
                    chatMessage.time = documentChange.getDocument().getDate("Time");
                    chatMessage.dateObject = documentChange.getDocument().getDate("Time");
                    Message.add(chatMessage);
                }
            }
            Collections.sort(Message,(obj1,obj2)->obj1.dateObject.compareTo(obj2.dateObject));
            if(count == 0)
            {
                chatAdapter.notifyDataSetChanged();
            }
            else {
                chatAdapter.notifyItemRangeInserted(Message.size(),Message.size());
                recycler_view_message.smoothScrollToPosition(Message.size()-1);
            }
            recycler_view_message.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case (R.id.btn_back):{
                onBackPressed();
                //tartActivity(new Intent(getApplicationContext(),ChatHistoryActivity.class));
                break;
            }
            case (R.id.contact_info):{
                //view contact_info
                break;
            }
            case (R.id.btn_call):{
                //call contact
                break;
            }
            case (R.id.btn_video_call):{
                try {
                    URL serverURL = new URL("https://meet.jit.si");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                break;
            }
            case (R.id.btn_image):{
                //choose image to send
                break;
            }
            case (R.id.btn_micro):{
                //send image by voice
                break;
            }
            case (R.id.btn_emoji):{
                popup.toggle();
                break;
            }
            case (R.id.btn_send):{
                SendMes();
                break;
            }
        }
    }

    private String getReadableDateTime(Date date)
    {
        return new SimpleDateFormat("MMMM dd,yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
}
