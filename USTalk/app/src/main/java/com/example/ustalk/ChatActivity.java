package com.example.ustalk;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ustalk.models.ChatMessage;
import com.example.ustalk.models.User;
import com.example.ustalk.network.ApiClient;
import com.example.ustalk.network.ApiService;
import com.example.ustalk.utilities.CurrentUserDetails;
import com.example.ustalk.utilities.PreferenceManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.vanniktech.emoji.EmojiPopup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener,EventListener {
    private ArrayList<ChatMessage> Message;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    HashMap<String, String> headers = new HashMap<>();
    ImageView avatar, btn_back, btn_call, btn_video_call, btn_image, btn_micro, btn_emoji, btn_send;
    TextView name;
    EditText edit_chat;
    RecyclerView recycler_view_message;
    LinearLayout contact_info;
    String receiveID;
    String receiveimage;
    String receivename;
    EmojiPopup popup;
    ImageView chat_background;
    ImageView[] toolbarListView, make_message_fieldListView;
    boolean isKeyboardShowing;
    ConstraintLayout chat_view;
    ScrollView chat_box_scrollview;

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
        chat_background = (ImageView) findViewById(R.id.chat_background);
        chat_view = (ConstraintLayout) findViewById(R.id.chat_view);
        chat_box_scrollview = (ScrollView) findViewById(R.id.chat_box_scrollview);

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

        //add Views to 2 list Views
        toolbarListView = new ImageView[3];
        toolbarListView[0] = btn_back;
        toolbarListView[1] = btn_call;
        toolbarListView[2] = btn_video_call;

        make_message_fieldListView = new ImageView[4];
        make_message_fieldListView[0] = btn_image;
        make_message_fieldListView[1] = btn_micro;
        make_message_fieldListView[2] = btn_emoji;
        make_message_fieldListView[3] = btn_send;

        cloneMessengerChatBox();
    }
    private void init()
    {
        preferenceManager = new PreferenceManager(getApplicationContext());
        Message = new ArrayList<>();
        chatAdapter = new ChatAdapter(this,Message,preferenceManager.getString("UID"));
        recycler_view_message.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
        headers.put("Authorization", "key=AAAAfFAKHSg:APA91bFihyTKsgfLDvBAymYxZbZvsLb4Rax7iEx7imaxejEFcefc36Q9PSTSUa2KuzO_LOe12XBo09CmAZnGfVuK0SegeWcdVx0gahWyiq8MM3G_wd-lXAtqJEfpgUlKgYsNtDxWKqEb");
        headers.put("Content-Type", "application-json");
        //"registration_ids"
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
        String message = edit_chat.getText().toString();
        HashMap<String,Object> mes = new HashMap<>();
        mes.put("senderID",preferenceManager.getString("UID"));
        mes.put("RecceiveID",receiveID);
        mes.put("Message", message);
        mes.put("Time",new Date());
        database.collection("chat").add(mes);
        edit_chat.setText(null);
        System.out.println(preferenceManager.getString("UID"));
        sendNotification(message);
    }

    private void sendNotification(String message) {
        try {
            JSONArray tokens = new JSONArray();
            //tokens.put(); receiver token

            User me = CurrentUserDetails.getInstance().getUser();
            JSONObject data = new JSONObject();
            data.put("uid", preferenceManager.getString("UID"));
            data.put("name", me.name);
            data.put("token", me.token);
            data.put("message", message);

            JSONObject body = new JSONObject();
            body.put("data", data);
            body.put("token", tokens);

            String jsonString = body.toString();
            ApiClient.getClient().create(ApiService.class)
                    .sendMessage(headers, jsonString).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (!response.isSuccessful()) {
                        Log.e("notification", String.valueOf(response.code()));
                        return;
                    }
                    String resBody = response.body();
                    if (resBody != null) {
                        try {
                            JSONObject resJson = new JSONObject(resBody);
                            if (resJson.getInt("failure") == 1) {
                                JSONArray results = resJson.getJSONArray("results");
                                JSONObject err = (JSONObject) results.get(0);
                                Log.e("notification", err.getString("error"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e("notification", call.toString());
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    public void changeImageViewTintColor(Context context, ImageView[] listView, int color){
        for (ImageView imageView : listView) {
            imageView.setColorFilter(context.getColor(color), PorterDuff.Mode.MULTIPLY);
        }
    }

    public void cloneMessengerChatBox(){
        //set image for main_background and prevent effect of adjustResize on it
        chat_background.setImageResource(R.drawable.chat_background);
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        chat_background.getLayoutParams().height = displayMetrics.heightPixels;

        //set tint color for all button on toolbar
        changeImageViewTintColor(
                getApplicationContext(), toolbarListView, R.color.purple_200);

        //set tint color for all button on make_message_field when size of layout changed
        isKeyboardShowing = false;
        chat_view.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect rect = new Rect();
                        chat_view.getWindowVisibleDisplayFrame(rect);
                        int screenHeight = chat_view.getRootView().getHeight();
                        int keypadHeight = screenHeight - rect.bottom;
                        int color = R.color.purple_500;
                        if (keypadHeight > screenHeight * 0.15){
                            if (!isKeyboardShowing){
                                isKeyboardShowing = true;
                                color = R.color.purple_500;
                            }
                        }
                        else{
                            if (isKeyboardShowing){
                                isKeyboardShowing = false;
                                color = R.color.purple_500;
                            }
                        }
                        changeImageViewTintColor(
                                getApplicationContext(), make_message_fieldListView, color);
                    }
                });

        //handle chat box behaviour when keyboard appears
        View last_child = chat_box_scrollview.getChildAt(chat_box_scrollview.getChildCount() - 1);
        chat_box_scrollview.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        chat_box_scrollview.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                chat_box_scrollview.smoothScrollTo(0, last_child.getBottom());
                            }
                        }, 150);
                    }
                });
    }
}
