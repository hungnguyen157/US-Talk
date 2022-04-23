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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.bumptech.glide.Glide;
import com.example.ustalk.models.ChatMessage;
import com.example.ustalk.models.User;
import com.example.ustalk.network.ApiClient;
import com.example.ustalk.network.ApiService;
import com.example.ustalk.utilities.CurrentUserDetails;
import com.example.ustalk.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.vanniktech.emoji.EmojiPopup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends OnlineActivity implements View.OnClickListener,EventListener {
    private ArrayList<ChatMessage> Message;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    HashMap<String, String> headers = new HashMap<>();
    CircleImageView avatar;
    ImageView btn_back, btn_call, btn_video_call, btn_image, btn_micro, btn_emoji, btn_send;
    TextView name;
    EditText edit_chat;
    RecyclerView recycler_view_message;
    String receiveID, receiveimage, receivename;
    EmojiPopup popup;
    ImageView chat_background, online_signal;
    ImageView[] toolbarListView, make_message_fieldListView;
    boolean isKeyboardShowing;
    ConstraintLayout contact_info, chat_view, toolbar, make_message_field;
    ScrollView chat_box_scrollview;
    private int IMAGE_GALLERY_REQUEST = 3;
    private Uri imageUri;
    String getReceiveimage;
    BackgroundAwareLayout chat_box_parent;
    private String receiveToken;
    private boolean sentMessage = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //get widgets
        name = findViewById(R.id.name);
        avatar = findViewById(R.id.avatar);
        btn_back = findViewById(R.id.btn_back);
        btn_call = findViewById(R.id.btn_call);
        btn_video_call = findViewById(R.id.btn_video_call);
        btn_image = findViewById(R.id.btn_image);
        btn_micro = findViewById(R.id.btn_micro);
        btn_emoji = findViewById(R.id.btn_emoji);
        btn_send = findViewById(R.id.btn_send);
        edit_chat = findViewById(R.id.edit_chat);
        recycler_view_message = findViewById(R.id.recycler_view_message);
        contact_info = findViewById(R.id.contact_info);
        chat_background = findViewById(R.id.chat_background);
        chat_view = findViewById(R.id.chat_view);
        toolbar = findViewById(R.id.toolbar);
        make_message_field = findViewById(R.id.make_message_field);
        chat_box_scrollview = findViewById(R.id.chat_box_scrollview);
        online_signal = findViewById(R.id.online_signal);
        chat_box_parent = findViewById(R.id.chat_box_parent);

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

        init();
        loadReceiverDetails();

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

        ListenMes();
    }
    private void init()
    {
        preferenceManager = new PreferenceManager(getApplicationContext());
        Message = new ArrayList<>();
        chatAdapter = new ChatAdapter(this,Message,preferenceManager.getString("UID"));
        recycler_view_message.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
        headers.put("Authorization", "key=AAAAfFAKHSg:APA91bFihyTKsgfLDvBAymYxZbZvsLb4Rax7iEx7imaxejEFcefc36Q9PSTSUa2KuzO_LOe12XBo09CmAZnGfVuK0SegeWcdVx0gahWyiq8MM3G_wd-lXAtqJEfpgUlKgYsNtDxWKqEb");
        headers.put("Content-Type", "application/json");
        //"registration_ids"
    }
    private void loadReceiverDetails()
    {
        receiveID = getIntent().getStringExtra("receiveID");

        if (receiveID.equals(preferenceManager.getString("UID"))){
            btn_call.setVisibility(View.INVISIBLE);
            btn_call.setClickable(false);
            btn_video_call.setVisibility(View.INVISIBLE);
            btn_video_call.setClickable(false);
        }

        database.collection("users").document(receiveID).addSnapshotListener(new com.google.firebase.firestore.EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("online", error.getMessage());
                    return;
                }
                if (value != null && value.exists()) {
                    boolean status = value.getBoolean("online");
                    receivename = value.getString("name");
                    receiveimage = value.getString("imageProfile");
                    receiveToken = value.getString("token");
                    name.setText(receivename);
                    Glide.with(getApplicationContext()).load(receiveimage).into(avatar);
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
        String message = edit_chat.getText().toString();HashMap<String, Object> mes = new HashMap<>();
        mes.put("senderID", preferenceManager.getString("UID"));
        mes.put("RecceiveID", receiveID);
        mes.put("Message", message);
        mes.put("Time", new Date());
        mes.put("sendimage", false);
        mes.put("senderFeeling", -1);
        mes.put("receiverFeeling", -1);
        database.collection("chat").add(mes);
        edit_chat.setText(null);
        System.out.println(preferenceManager.getString("UID"));
        sendNotification(message);
        sentMessage = true;
    }

    private void sendNotification(String message) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiveToken);

            User me = CurrentUserDetails.getInstance().getUser();
            JSONObject data = new JSONObject();
            data.put("uid", preferenceManager.getString("UID"));
            data.put("message", message);

            JSONObject body = new JSONObject();
            body.put("data", data);
            body.put("registration_ids", tokens);

            String jsonString = body.toString();
            ApiClient.getClient().create(ApiService.class)
                    .sendMessage(headers, jsonString).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
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
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
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
                    } catch (Exception ex) {
                        System.out.println("Not OK");
                        chatMessage.feeling = -1;
                        chatMessage.senderFeeling = -1;
                        chatMessage.receiverFeeling = -1;
                    }
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
                if (sentMessage) {
                    recycler_view_message.smoothScrollToPosition(Message.size() - 1);
                    sentMessage = false;
                }
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
                mes.put("senderFeeling", -1);
                mes.put("receiverFeeling", -1);
                database.collection("chat").add(mes);
                sentMessage = true;
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
                break;
            }
            case (R.id.contact_info):{
                Toast.makeText(getApplicationContext(),
                        "Chức năng này hiện chưa khả dụng",
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case (R.id.btn_call):{
                if (receiveToken == null || receiveToken.trim().isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            receivename + " không sẵn sàng cho cuộc gọi âm thanh vào lúc này",
                            Toast.LENGTH_SHORT).show();
                }
                else{
                    Intent intentAudioCall = new Intent(getApplicationContext(), OutcommingCallActivity.class);
                    intentAudioCall.putExtra("uid", receiveID);
                    intentAudioCall.putExtra("name", receivename);
                    intentAudioCall.putExtra("avatar", receiveimage);
                    intentAudioCall.putExtra("token", receiveToken);
                    intentAudioCall.putExtra("type", "audio");
                    startActivity(intentAudioCall);
                }
                break;
            }
            case (R.id.btn_video_call):{
                if (receiveToken == null || receiveToken.trim().isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            receivename + " không sẵn sàng cho cuộc gọi video vào lúc này",
                            Toast.LENGTH_SHORT).show();
                }
                else{
                    Intent intentVideoCall = new Intent(getApplicationContext(), OutcommingCallActivity.class);
                    intentVideoCall.putExtra("uid", receiveID);
                    intentVideoCall.putExtra("name", receivename);
                    intentVideoCall.putExtra("avatar", receiveimage);
                    intentVideoCall.putExtra("token", receiveToken);
                    intentVideoCall.putExtra("type", "video");
                    startActivity(intentVideoCall);
                }
                break;
            }
            case (R.id.btn_image):{
                openGallery();
                break;
            }
            case (R.id.btn_micro):{
                Toast.makeText(getApplicationContext(),
                        "Chức năng này vẫn đang được cài đặt",
                        Toast.LENGTH_SHORT).show();
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

        //remove animation when add new message to recycler_view_message
        RecyclerView.ItemAnimator animator = recycler_view_message.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        //handle chat box behaviour when keyboard appears
        recycler_view_message.setFocusable(false);
        recycler_view_message.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = make_message_field.getTop() - toolbar.getBottom();
                if (recycler_view_message.getHeight() >= height && !isKeyboardShowing){
                    recycler_view_message.getLayoutParams().height = height;
                    recycler_view_message.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

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
                        }, 50);
                    }
                });
    }
}
