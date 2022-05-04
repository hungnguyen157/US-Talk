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
import android.media.AudioFormat;
import android.media.AudioRecord;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
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
    private String userID, receiveID, receiveimage, receivename, receiveToken;
    EmojiPopup popup;
    ImageView chat_background, online_signal;
    ImageView[] toolbarListView, make_message_fieldListView;
    boolean isKeyboardShowing;
    ConstraintLayout contact_info, chat_view, toolbar, make_message_field;
    ScrollView chat_box_scrollview;
    private int IMAGE_GALLERY_REQUEST = 3;
    private Uri imageUri;
    BackgroundAwareLayout chat_box_parent;
    private boolean sentMessage = false;
    Dialog recordVoiceDialog;
    String audioSentPath, audioTempPath, audioTempPath2, audioRawPath;
    AudioRecord voiceRecorder;
    private Thread recordingThread;
    private final int RECORDER_BPP = 16;
    private final int RECORDER_SAMPLE_RATE = 8000;
    private final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private final int bufferSize = AudioRecord.getMinBufferSize(
                                    RECORDER_SAMPLE_RATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
    Chronometer time, timeRecord;
    ImageButton btn_play_or_stop;
    ImageView btnStartPauseRecord;
    boolean isRecording = false, isConcatenated = false;
    long recorderPosition = 0;
    private AudioPlayerService audioPlayerService;
    private Handler handler;
    private Runnable updater;
    SeekBar sound_seekbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        initSomeOtherVariables();
        loadReceiverDetails();

        cloneMessengerChatBox();

        ListenMes();
    }
    private void initViews() {
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
        popup = EmojiPopup.Builder.fromRootView(chat_view).build(edit_chat);
    }
    private void initSomeOtherVariables() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        userID = preferenceManager.getString("UID");
        Message = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, Message, userID);
        recycler_view_message.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
        headers.put("Authorization", "key=AAAAfFAKHSg:APA91bFihyTKsgfLDvBAymYxZbZvsLb4Rax7iEx7imaxejEFcefc36Q9PSTSUa2KuzO_LOe12XBo09CmAZnGfVuK0SegeWcdVx0gahWyiq8MM3G_wd-lXAtqJEfpgUlKgYsNtDxWKqEb");
        headers.put("Content-Type", "application/json");
        //"registration_ids"

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
    }
    private void loadReceiverDetails()
    {
        receiveID = getIntent().getStringExtra("receiveID");

        if (receiveID.equals(userID)){
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

    //send text message function
    private void sendTextMessage()
    {
        String message = edit_chat.getText().toString();HashMap<String, Object> mes = new HashMap<>();
        sendMessage(message, false, false);
    }

    //get image message functions
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
                String getReceiveimage = encodeImage(bitmap);
                sendImageMessage(getReceiveimage);
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
    //send image message function
    private void sendImageMessage(String getReceiveimage) {
        sendMessage(getReceiveimage, true, false);
    }

    //send voice message function
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
                sendMessage(voiceUrl, false, true);
            }
        });
    }

    //send message function
    private void sendMessage(String message, boolean isImage, boolean isVoice)
    {
        HashMap<String, Object> mes = new HashMap<>();
        mes.put("senderID", userID);
        mes.put("RecceiveID", receiveID);
        mes.put("Message", message);
        mes.put("Time", new Date());
        mes.put("sendimage", isImage);
        mes.put("sendvoice", isVoice);
        mes.put("senderFeeling", -1);
        mes.put("receiverFeeling", -1);
        database.collection("chat").add(mes);
        edit_chat.setText(null);
        System.out.println(userID);
        if (isImage) sendNotification("Đã gửi một hình ảnh");
        else if (isVoice) sendNotification("Đã gửi một tin nhắn âm thanh");
        else sendNotification(message);
        sentMessage = true;
    }

    //send notification whenever there is a new message sent
    private void sendNotification(String message) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiveToken);

            User me = CurrentUserDetails.getInstance().getUser();
            JSONObject data = new JSONObject();
            data.put("uid", userID);
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

    private void ListenMes() {
        if (receiveID.equals(userID)) {
            database.collection("chat")
                    .whereEqualTo("senderID", userID)
                    .whereEqualTo("RecceiveID", receiveID)
                    .addSnapshotListener(eventListener);
        }
        else {
            database.collection("chat")
                    .whereEqualTo("senderID", userID)
                    .whereEqualTo("RecceiveID", receiveID)
                    .addSnapshotListener(eventListener);
            database.collection("chat")
                    .whereEqualTo("senderID", receiveID)
                    .whereEqualTo("RecceiveID", userID)
                    .addSnapshotListener(eventListener);
        }
    }
    private final com.google.firebase.firestore.EventListener<QuerySnapshot> eventListener =(value, error) ->{
        if(error != null) {
            return ;
        }
        if(value != null) {
            int count = Message.size();
            ArrayList<Integer> modifiedPositions = new ArrayList<>();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
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

    private Intent createCallIntent(String type){
        Intent intentCall = new Intent(getApplicationContext(), OutcommingCallActivity.class);
        intentCall.putExtra("uid", receiveID);
        intentCall.putExtra("name", receivename);
        intentCall.putExtra("avatar", receiveimage);
        intentCall.putExtra("token", receiveToken);
        intentCall.putExtra("type", type);
        return intentCall;
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
                    Intent intentAudioCall = createCallIntent("audio");
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
                    Intent intentVideoCall = createCallIntent("video");
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
                if (!edit_chat.getText().toString().trim().isEmpty()) sendTextMessage();
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
        String fileName = "USTalkVoiceMess_" + LocalDateTime.now().toString();
        audioSentPath = externalStorageDirectory + "/Music/" + fileName + ".wav";
        audioTempPath = externalStorageDirectory + "/Music/" + "Temp" + fileName + ".wav";
        audioTempPath2 = externalStorageDirectory + "/Music/" + "Temp2" + fileName + ".wav";
        audioRawPath = externalStorageDirectory + "/Music/" + "Raw" + fileName + ".raw";

        //start recording
        try {
            startRecord();
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

        //setup recorder and start recording if permission is granted
        if (ActivityCompat.checkSelfPermission(
                ChatActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            voiceRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    RECORDER_SAMPLE_RATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);
            voiceRecorder.startRecording();
            timeRecord.setBase(SystemClock.elapsedRealtime() - recorderPosition);
            timeRecord.start();
            isRecording = true;
            isConcatenated = false;
            recordingThread = new Thread(this::writeAudioDataToRawFile, "AudioRecorder Thread");
            recordingThread.start();
        }
    }
    private void writeAudioDataToRawFile() {
        byte[] data = new byte[bufferSize];
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(audioRawPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (fos != null) {
            int read;
            while (isRecording) {
                read = voiceRecorder.read(data, 0, bufferSize);
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        fos.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void pauseRecord() {
        //pause recording
        clearVoiceRecorder();
        recorderPosition = SystemClock.elapsedRealtime() - timeRecord.getBase();
        timeRecord.stop();

        //change accessibility of some widgets
        timeRecord.setVisibility(View.INVISIBLE);
        btn_play_or_stop.setEnabled(true);
        sound_seekbar.setEnabled(true);
        time.setVisibility(View.VISIBLE);
        time.setBase(SystemClock.elapsedRealtime() - recorderPosition);
        btnStartPauseRecord.setImageResource(R.drawable.voice_icon);

        //convert recorded raw audio to .wav file to preview if need to
        convertRawToWaveFile();

        //initial audio player service
        audioPlayerService = new AudioPlayerService(Uri.fromFile(new File(audioSentPath)).toString());
        handler = new Handler();
        updater = this::updateSeekerBar;
    }
    private void convertRawToWaveFile() {
        FileInputStream fis;
        FileOutputStream fos;
        long totalAudioLen, totalDataLen;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLE_RATE * channels / 8;

        byte[] data = new byte[bufferSize];

        try {
            fis = new FileInputStream(audioRawPath);
            fos = new FileOutputStream(audioSentPath, true);
            totalAudioLen = fis.getChannel().size() + fos.getChannel().size();
            totalDataLen = totalAudioLen + 44;

            WriteWaveFileHeader(totalAudioLen, totalDataLen, channels, byteRate, audioSentPath);

            while (fis.read(data) != -1) {
                fos.write(data);
            }

            fis.close();
            fos.close();
            isConcatenated = true;
        } catch (IOException e) {
            e.printStackTrace();
            isConcatenated = false;
        }
    }
    private void WriteWaveFileHeader(long totalAudioLen, long totalDataLen, int channels, long byteRate,
                                     String outFileName) {

        byte[] header = new byte[44];
        // RIFF/WAVE header
        header[0] = 'R'; header[1] = 'I'; header[2] = 'F'; header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W'; header[9] = 'A'; header[10] = 'V'; header[11] = 'E';

        // 'fmt ' chunk
        header[12] = 'f'; header[13] = 'm'; header[14] = 't'; header[15] = ' ';

        // 4 bytes: size of 'fmt ' chunk
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0;

        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (RECORDER_SAMPLE_RATE & 0xff);
        header[25] = (byte) ((RECORDER_SAMPLE_RATE >> 8) & 0xff);
        header[26] = (byte) ((RECORDER_SAMPLE_RATE >> 16) & 0xff);
        header[27] = (byte) ((RECORDER_SAMPLE_RATE >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd'; header[37] = 'a'; header[38] = 't'; header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        try(RandomAccessFile rFile = new RandomAccessFile(outFileName, "rw")) {
            rFile.seek(0);
            rFile.write(header, 0, 44);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void resumeRecord() throws IOException {
        //clear the audio player service
        clearAudioPlayerService();

        //restart recorder
        startRecord();
    }
    private void stopRecord(boolean forSend) throws IOException {
        //clear audio player service
        clearAudioPlayerService();

        //stop recording
        timeRecord.stop();
        recorderPosition = 0;
        if (isRecording) {
            clearVoiceRecorder();
        }

        //check if recording was stopped to send message
        if (forSend) {
            if (!isConcatenated) convertRawToWaveFile();
            sendVoiceMessage();
        }

        //delete temp file (which used to store audio file while recording)
        File mainFile = new File(audioSentPath);
        if (mainFile.exists()) mainFile.delete();
        File rawFile = new File(audioRawPath);
        if (rawFile.exists()) rawFile.delete();

        recordVoiceDialog.dismiss();
    }
    private void resetRecord() throws IOException {
        //clear audio player service
        clearAudioPlayerService();

        File mainFile = new File(audioSentPath);
        if (mainFile.exists()) mainFile.delete();

        //reset recorder
        if (isRecording) {
            clearVoiceRecorder();
        }
        recorderPosition = 0;
        startRecord();
    }
    private void clearVoiceRecorder() {
        isRecording = false;
        voiceRecorder.stop();
        voiceRecorder.release();
        recordingThread = null;
    }
    private void clearAudioPlayerService() {
        btn_play_or_stop.setImageResource(R.drawable.ic_round_play_arrow_24);
        sound_seekbar.setProgress(100);
        time.stop();
        time.setBase(SystemClock.elapsedRealtime() - recorderPosition);

        if (audioPlayerService != null) {
            handler.removeCallbacks(updater);
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
