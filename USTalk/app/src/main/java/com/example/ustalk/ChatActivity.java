package com.example.ustalk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ustalk.models.ChatMessage;
import com.example.ustalk.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.vanniktech.emoji.EmojiPopup;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Locale;

public class ChatActivity extends OnlineActivity implements View.OnClickListener,EventListener {
    private ArrayList<ChatMessage> Message;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    ImageView avatar, btn_back, btn_call, btn_video_call, btn_image, btn_micro, btn_emoji, btn_send;
    TextView name;
    EditText edit_chat;
    RecyclerView recycler_view_message;
    String receiveID, receiveimage, receivename;
    EmojiPopup popup;
    ImageView chat_background, online_signal;
    ImageView[] toolbarListView, make_message_fieldListView;
    boolean isKeyboardShowing;
    ConstraintLayout contact_info, chat_view;
    ScrollView chat_box_scrollview;
    private int IMAGE_GALLERY_REQUEST = 3;
    private Uri imageUri;
    String getReceiveimage;
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
        contact_info = (ConstraintLayout) findViewById(R.id.contact_info);
        chat_background = (ImageView) findViewById(R.id.chat_background);
        chat_view = (ConstraintLayout) findViewById(R.id.chat_view);
        chat_box_scrollview = (ScrollView) findViewById(R.id.chat_box_scrollview);
        online_signal = findViewById(R.id.online_signal);

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
        popup = EmojiPopup.Builder.fromRootView(chat_view).build(edit_chat);
//        btn_emoji.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                popup.toggle();
//            }
//        });
        //btn_send.setOnClickListener(v->SendMes());

        init();
        loadReceiverDetails();
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
    }
    private void loadReceiverDetails()
    {
        receiveID = getIntent().getStringExtra("receiveID");
        receivename = getIntent().getStringExtra("name");
        receiveimage=getIntent().getStringExtra("imageProfile");
        name.setText(receivename);
        Glide.with(ChatActivity.this).load(receiveimage).into(avatar);

        database.collection("users").document(receiveID).addSnapshotListener(new com.google.firebase.firestore.EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("online", error.getMessage());
                    return;
                }
                if (value != null && value.exists()) {
                    boolean status = value.getBoolean("online");
                    if (status) {
                        online_signal.setVisibility(View.VISIBLE);
                    }
                    else{
                        online_signal.setVisibility(View.INVISIBLE);
                    }
                }
                else Log.e("online", "NULL value");
            }
        });
    }
    public void SendMes()
    {
            HashMap<String, Object> mes = new HashMap<>();
            mes.put("senderID", preferenceManager.getString("UID"));
            mes.put("RecceiveID", receiveID);
            mes.put("Message", edit_chat.getText().toString());
            mes.put("Time", new Date());
            mes.put("sendimage",false);
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
                    chatMessage.sendimage = documentChange.getDocument().getBoolean("sendimage");
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
    private void openGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,IMAGE_GALLERY_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK&&data!=null){
            imageUri = data.getData();
            try {
                InputStream is = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                getReceiveimage = encodeImage(bitmap);
                ChatMessage chatMessage = new ChatMessage();
                HashMap<String, Object> mes = new HashMap<>();
                mes.put("senderID", preferenceManager.getString("UID"));
                mes.put("RecceiveID", receiveID);
                mes.put("Message", getReceiveimage);
                mes.put("Time", new Date());
                mes.put("sendimage",true);
                database.collection("chat").add(mes);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    private String encodeImage(Bitmap bitmap)
    {
        int previewWidth = 350;
        int previewHeight = bitmap.getHeight() *previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }
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
                openGallery();
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
        return new SimpleDateFormat("hh:mm", Locale.getDefault()).format(date);
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
