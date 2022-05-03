package com.example.ustalk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.bumptech.glide.Glide;
import com.example.ustalk.models.ChatMessage;
import com.example.ustalk.models.User;
import com.example.ustalk.network.ApiClient;
import com.example.ustalk.network.ApiService;
import com.example.ustalk.utilities.AudioPlayerService;
import com.example.ustalk.utilities.CurrentUserDetails;
import com.example.ustalk.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vanniktech.emoji.EmojiPopup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends OnlineActivity implements View.OnClickListener,EventListener {
    private ArrayList<ChatMessage> Message;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    int unloadedTypeMessNum = 2;
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
    Dialog recordVoiceDialog;
    String audioSentPath, audioTempPath, audioTempPath2;
    MediaRecorder mediaRecorder;
    Chronometer time, timeRecord;
    ImageButton btn_play_or_stop;
    ImageView btnStartPauseRecord;
    boolean isRecording = false, isConcated = false;
    long recorderPosition;
    private AudioPlayerService audioPlayerService;
    private Handler handler;
    private Runnable updater;
    SeekBar sound_seekbar;
    long startRecord;

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
        mes.put("sendvoice", false);
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
            ArrayList<Integer> modifiedPositions = new ArrayList<>();
            for (DocumentChange documentChange : value.getDocumentChanges())
            {
                DocumentChange.Type type = documentChange.getType();
                if(type == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = ChatMessage.fromDocumentChange(documentChange);
                    Message.add(chatMessage);
                }
                else if (type == DocumentChange.Type.MODIFIED) {
                    ChatMessage chatMessage = ChatMessage.fromDocumentChange(documentChange);
                    ChatMessage chatMessageInMessage = Message.stream()
                            .filter(chatMessage1 -> chatMessage.id.equals(chatMessage1.id))
                            .findFirst().orElse(null);
                    Message.set(Message.indexOf(chatMessageInMessage), chatMessage);
                    modifiedPositions.add(Message.indexOf(chatMessage));
                }
            }
            Message.sort((obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if(count == 0) {
                chatAdapter.notifyDataSetChanged();
            }
            else {
                int size = Message.size();
                if (size != count) {
                    chatAdapter.notifyItemRangeInserted(Message.size(),Message.size());
                    if (sentMessage || unloadedTypeMessNum > 0) {
                        recycler_view_message.smoothScrollToPosition(Message.size() - 1);
                        sentMessage = false;
                    }
                }
                for (Integer modifiedPosition : modifiedPositions) {
                    System.out.println(modifiedPosition);
                    chatAdapter.notifyItemChanged(modifiedPosition);
                }
            }
            recycler_view_message.setVisibility(View.VISIBLE);

            if (unloadedTypeMessNum > 0) unloadedTypeMessNum--;
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
                mes.put("sendvoice", false);
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
            case (R.id.btn_back): {
                onBackPressed();
                break;
            }
            case (R.id.contact_info): {
                Toast.makeText(getApplicationContext(),
                        "Chức năng này hiện chưa khả dụng",
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case (R.id.btn_call): {
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
            case (R.id.btn_video_call): {
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
            case (R.id.btn_image): {
                openGallery();
                break;
            }
            case (R.id.btn_micro): {
                if (!checkRecordAudioPermission()) {
                    recordVoiceDialog = new Dialog(ChatActivity.this);
                    showRecordVoiceDialog();
                }
                else requestRecordAudioPermission();
                break;
            }
            case (R.id.btn_emoji): {
                popup.toggle();
                break;
            }
            case (R.id.btn_send): {
                if (!edit_chat.getText().toString().trim().isEmpty()) SendMes();
                break;
            }
            case (R.id.btn_play_or_stop): {
                previewVoiceMessage();
                break;
            }
            case (R.id.btnStartPauseRecord): {
                if (isRecording) {
                    pauseRecord();
                }
                else {
                    try {
                        resumeRecord();
                    } catch (IOException e) {
                        e.printStackTrace();
                        recordVoiceDialog.dismiss();
                    }
                }
                break;
            }
            case (R.id.btnCancelRecord): {
                try {
                    stopRecord(false);
                } catch (IOException e) {
                    e.printStackTrace();
                    recordVoiceDialog.dismiss();
                }
                break;
            }
            case (R.id.btnResetRecord): {
                try {
                    resetRecord();
                } catch (Exception e) {
                    e.printStackTrace();
                    recordVoiceDialog.dismiss();
                }
                break;
            }
            case (R.id.btnSendVoiceMessage): {
                try {
                    stopRecord(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    recordVoiceDialog.dismiss();
                }
                break;
            }
        }
    }

    //check needed permission
    private boolean checkRecordAudioPermission() {
        boolean isRecordingNotOK = ContextCompat.checkSelfPermission(
                ChatActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED;
        boolean isWriteExternalNotOK = ContextCompat.checkSelfPermission(
                ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED;
        boolean isReadExternalNotOK = ContextCompat.checkSelfPermission(
                ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED;
        return isRecordingNotOK || isWriteExternalNotOK || isReadExternalNotOK;
    }
    private void requestRecordAudioPermission() {
        ActivityCompat.requestPermissions(
                ChatActivity.this,
                new String[]{Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                165);
    }

    //make chat box look likes Messenger chat box
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

    //handle record voice message part
    @SuppressLint("ClickableViewAccessibility")
    private void showRecordVoiceDialog() {
        recordVoiceDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        recordVoiceDialog.setContentView(R.layout.record_message_layout);
        recordVoiceDialog.setCanceledOnTouchOutside(false);

        ImageView btnCancelRecord, btnResetRecord, btnSend;
        btn_play_or_stop = recordVoiceDialog.findViewById(R.id.btn_play_or_stop);
        sound_seekbar = recordVoiceDialog.findViewById(R.id.sound_seekbar);
        time = recordVoiceDialog.findViewById(R.id.time);
        timeRecord = recordVoiceDialog.findViewById(R.id.timeRecord);
        btnStartPauseRecord = recordVoiceDialog.findViewById(R.id.btnStartPauseRecord);
        btnCancelRecord = recordVoiceDialog.findViewById(R.id.btnCancelRecord);
        btnResetRecord = recordVoiceDialog.findViewById(R.id.btnResetRecord);
        btnSend = recordVoiceDialog.findViewById(R.id.btnSendVoiceMessage);

        btn_play_or_stop.setOnClickListener(this);
        btnStartPauseRecord.setOnClickListener(this);
        btnCancelRecord.setOnClickListener(this);
        btnResetRecord.setOnClickListener(this);
        btnSend.setOnClickListener(this);

        sound_seekbar.setMax(100);
        sound_seekbar.setOnTouchListener((view, motionEvent) -> {
            SeekBar seekBar = (SeekBar) view;
            int playPosition = (int) ((audioPlayerService.getDuration() / 100) * seekBar.getProgress());
            audioPlayerService.seekTo(playPosition);
            time.setBase(SystemClock.elapsedRealtime() - audioPlayerService.getCurrentPosition());
            return false;
        });

        String externalStorageDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = "USTalkVoiceMess_" + LocalDateTime.now().toString() + ".wav";
        audioSentPath = externalStorageDirectory + "/Music/" + fileName;
        audioTempPath = externalStorageDirectory + "/Music/" + "Temp" + fileName;
        audioTempPath2 = externalStorageDirectory + "/Music/" + "Temp2" + fileName;

        //setup audio recorder
        mediaRecorder = new MediaRecorder();
        try {
            startRecord();
            startRecord = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
            recordVoiceDialog.dismiss();
        }

        recordVoiceDialog.show();
        recordVoiceDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.WRAP_CONTENT);
        recordVoiceDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        recordVoiceDialog.getWindow().setGravity(Gravity.BOTTOM);
    }
    private void startRecord() throws IOException {
        //change accessibility of some widgets
        btn_play_or_stop.setEnabled(false);
        sound_seekbar.setProgress(100);
        sound_seekbar.setEnabled(false);
        time.setVisibility(View.INVISIBLE);
        timeRecord.setVisibility(View.VISIBLE);
        btnStartPauseRecord.setImageResource(R.drawable.ic_round_pause_24);

        //setup recorder for new record
        prepareAudioRecorder();

        //start recording
        mediaRecorder.start();
        recorderPosition = 0;
        timeRecord.setBase(SystemClock.elapsedRealtime() - recorderPosition);
        timeRecord.start();
        isRecording = true;
        isConcated = false;
    }
    private void prepareAudioRecorder() throws IOException {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(audioTempPath);
        mediaRecorder.prepare();
    }
    private void pauseRecord() {
        //pause recording
        isRecording = false;
        mediaRecorder.stop();
        recorderPosition = SystemClock.elapsedRealtime() - timeRecord.getBase();
        timeRecord.stop();

        //change accessibility of some widgets
        timeRecord.setVisibility(View.INVISIBLE);
        btn_play_or_stop.setEnabled(true);
        sound_seekbar.setEnabled(true);
        time.setVisibility(View.VISIBLE);
        time.setBase(SystemClock.elapsedRealtime() - recorderPosition);
        btnStartPauseRecord.setImageResource(R.drawable.voice_icon);

        //Create a temp copy audio file to preview
        try {
            concatenateToMainFile();
        } catch (IOException e) {
            e.printStackTrace();
            recordVoiceDialog.dismiss();
        }

        //initial audio player service
        audioPlayerService = new AudioPlayerService(Uri.fromFile(new File(audioSentPath)).toString());
        handler = new Handler();
        updater = new Runnable() {
            @Override
            public void run() {
                updateSeekerBar();
            }
        };
    }
    private void concatenateToMainFile() throws IOException {
        File mainFile = new File(audioSentPath);
        if (!mainFile.exists()) {
            Files.copy(new File(audioTempPath).toPath(),
                    new File(audioSentPath).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        else {
            //concatenate2AudioFile();
        }
        isConcated = true;
    }
    private void concatenate2AudioFile() {
        int RECORDER_SAMPLERATE = 0;
        try {
            String[] selection=new String[2];
            selection[0]=audioSentPath;
            selection[1]=audioTempPath;
            int length = selection.length;
            DataOutputStream amplifyOutputStream = new DataOutputStream(
                                    new BufferedOutputStream(new FileOutputStream(audioTempPath2)));
            DataInputStream[] mergeFilesStream = new DataInputStream[length];
            long[] sizes = new long[length];
            for (int i = 0; i < length; i++) {
                File file = new File(selection[i]);
                sizes[i] = (file.length() - 44) / 2;
            }
            for (int i = 0; i < length; i++) {
                mergeFilesStream[i] =new DataInputStream(
                        new BufferedInputStream(new FileInputStream(selection[i])));
                if (i == length - 1) {
                    mergeFilesStream[i].skip(24);
                    byte[] sampleRt = new byte[4];
                    mergeFilesStream[i].read(sampleRt);
                    ByteBuffer bbInt = ByteBuffer.wrap(sampleRt).order(ByteOrder.LITTLE_ENDIAN);
                    RECORDER_SAMPLERATE = bbInt.getInt();
                    mergeFilesStream[i].skip(16);
                }
                else {
                    mergeFilesStream[i].skip(44);
                }

            }

            for (int b = 0; b < length; b++) {
                for (int i = 0; i < (int)sizes[b]; i++) {
                    byte[] dataBytes = new byte[2];
                    try {
                        dataBytes[0] = mergeFilesStream[b].readByte();
                        dataBytes[1] = mergeFilesStream[b].readByte();
                    }
                    catch (EOFException e) {
                        amplifyOutputStream.close();
                    }
                    short dataInShort = ByteBuffer.wrap(dataBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    float dataInFloat= (float) dataInShort/37268.0f;

                    short outputSample = (short)(dataInFloat * 37268.0f);
                    byte[] dataFin = new byte[2];
                    dataFin[0] = (byte) (outputSample & 0xff);
                    dataFin[1] = (byte)((outputSample >> 8) & 0xff);
                    amplifyOutputStream.write(dataFin, 0 , 2);
                }
            }
            amplifyOutputStream.close();
            for (int i = 0; i < length; i++) {
                mergeFilesStream[i].close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        long size = 0;
        try {
            FileInputStream fileSize = new FileInputStream(audioTempPath2);
            size = fileSize.getChannel().size();
            fileSize.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //write header to new file
        final int RECORDER_BPP = 16;

        long datasize = size + 36;
        long byteRate = ((long) RECORDER_BPP * RECORDER_SAMPLERATE) / 8;
        long longSampleRate = RECORDER_SAMPLERATE;
        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (datasize & 0xff);
        header[5] = (byte) ((datasize >> 8) & 0xff);
        header[6] = (byte) ((datasize >> 16) & 0xff);
        header[7] = (byte) ((datasize >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) 1;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) ((RECORDER_BPP) / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (size & 0xff);
        header[41] = (byte) ((size >> 8) & 0xff);
        header[42] = (byte) ((size >> 16) & 0xff);
        header[43] = (byte) ((size >> 24) & 0xff);
        // out.write(header, 0, 44);

        try {
            //add header to concatenated new file
            RandomAccessFile rFile = new RandomAccessFile(audioTempPath2, "rw");
            rFile.seek(0);
            rFile.write(header);
            rFile.close();

            //copy to the main file
            Files.copy(new File(audioTempPath2).toPath(),
                    new File(audioSentPath).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            File tempConcatenatedFile = new File(audioTempPath2);
            tempConcatenatedFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte)(totalDataLen & 0xff);
        header[5] = (byte)((totalDataLen >> 8) & 0xff);
        header[6] = (byte)((totalDataLen >> 16) & 0xff);
        header[7] = (byte)((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte)(longSampleRate & 0xff);
        header[25] = (byte)((longSampleRate >> 8) & 0xff);
        header[26] = (byte)((longSampleRate >> 16) & 0xff);
        header[27] = (byte)((longSampleRate >> 24) & 0xff);
        header[28] = (byte)(byteRate & 0xff);
        header[29] = (byte)((byteRate >> 8) & 0xff);
        header[30] = (byte)((byteRate >> 16) & 0xff);
        header[31] = (byte)((byteRate >> 24) & 0xff);
        header[32] = (byte)(2 * 16 / 8);
        header[33] = 0;
        header[34] = 16;
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte)(totalAudioLen & 0xff);
        header[41] = (byte)((totalAudioLen >> 8) & 0xff);
        header[42] = (byte)((totalAudioLen >> 16) & 0xff);
        header[43] = (byte)((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }
    private void resumeRecord() throws IOException {
        //clear the audio player service
        clearAudioPlayerService();

        //change accessibility of some widgets
        btn_play_or_stop.setEnabled(false);
        sound_seekbar.setEnabled(false);
        time.setVisibility(View.INVISIBLE);
        timeRecord.setVisibility(View.VISIBLE);
        btnStartPauseRecord.setImageResource(R.drawable.ic_round_pause_24);

        //resume recording
        prepareAudioRecorder();
        mediaRecorder.start();
        timeRecord.setBase(SystemClock.elapsedRealtime() - recorderPosition);
        timeRecord.start();
        isRecording = true;
        isConcated = false;
    }
    private void stopRecord(boolean forSend) throws IOException {
        //clear audio player service
        clearAudioPlayerService();

        //stop recording
        timeRecord.stop();
        recorderPosition = 0;
        if (isRecording) {
            mediaRecorder.stop();
        }
        mediaRecorder.release();
        mediaRecorder = null;
        isRecording = false;

        //check if recording was stopped to send message
        if (forSend) {
            if (!isConcated) {
                concatenateToMainFile();
            }
            sendVoiceMessage();
        }

        //delete temp file (which used to store audio file while recording)
        File mainFile = new File(audioSentPath);
        if (mainFile.exists()) mainFile.delete();
        File tempFile = new File(audioTempPath);
        if (tempFile.exists()) tempFile.delete();
        recordVoiceDialog.dismiss();
    }
    private void sendVoiceMessage() {
        Uri uri = Uri.fromFile(new File(audioSentPath));
        StorageReference audioRef = FirebaseStorage.getInstance().getReference().
                child("VoiceMessages/"
                        + CurrentUserDetails.getInstance().getUid() + "_" + receiveID + "_"
                        + System.currentTimeMillis());
        audioRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!urlTask.isSuccessful());
                String voiceUrl = urlTask.getResult().toString();

                HashMap<String, Object> mes = new HashMap<>();
                mes.put("senderID", preferenceManager.getString("UID"));
                mes.put("RecceiveID", receiveID);
                mes.put("Message", voiceUrl);
                mes.put("Time", new Date());
                mes.put("sendimage",false);
                mes.put("sendvoice", true);
                mes.put("senderFeeling", -1);
                mes.put("receiverFeeling", -1);
                database.collection("chat").add(mes);
                sentMessage = true;
            }
        });
    }
    private void resetRecord() throws IOException {
        //clear audio player service
        clearAudioPlayerService();

        File mainFile = new File(audioSentPath);
        if (mainFile.exists()) mainFile.delete();

        //reset recorder
        if (isRecording) mediaRecorder.stop();
        mediaRecorder.reset();
        startRecord();
        isConcated = false;
    }
    private void clearAudioPlayerService() {
        sound_seekbar.setProgress(100);
        time.stop();
        time.setBase(SystemClock.elapsedRealtime() - recorderPosition);

        if (audioPlayerService != null) {
            audioPlayerService.clearPlayer();
            audioPlayerService = null;
        }
    }

    //preview voice message parts
    private void previewVoiceMessage(){
        if (audioPlayerService != null){
            if (audioPlayerService.isPlaying()){
                handler.removeCallbacks(updater);
                time.stop();
                audioPlayerService.pauseAudio();
                btn_play_or_stop.setImageResource(R.drawable.ic_round_play_arrow_24);
            }
            else{
                audioPlayerService.playAudio(new AudioPlayerService.OnPlayCallBack() {
                    @Override
                    public void onFinished() {
                        sound_seekbar.setProgress(100);
                        time.stop();
                        time.setBase(SystemClock.elapsedRealtime() - recorderPosition);
                        btn_play_or_stop.setImageResource(R.drawable.ic_round_play_arrow_24);
                        handler.removeCallbacks(updater);
                    }
                });
                btn_play_or_stop.setImageResource(R.drawable.ic_round_pause_24);
                time.setBase(SystemClock.elapsedRealtime() - audioPlayerService.getCurrentPosition());
                time.start();
                updateSeekerBar();
            }
        }
    }
    private void updateSeekerBar(){
        if (audioPlayerService.isPlaying()){
            sound_seekbar.setProgress(
                    (int)(((float) audioPlayerService.getCurrentPosition() / audioPlayerService.getDuration()) * 100));
            handler.postDelayed(updater, 1000);
        }
    }
}
