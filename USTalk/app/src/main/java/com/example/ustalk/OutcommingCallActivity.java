package com.example.ustalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.example.ustalk.models.User;
import com.example.ustalk.network.ApiClient;
import com.example.ustalk.network.ApiService;
import com.example.ustalk.utilities.Constants;
import com.example.ustalk.utilities.CurrentUserDetails;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutcommingCallActivity extends AppCompatActivity {
    //widgets variables
    ImageView callImageType, btn_cancel;
    CircleImageView avatar;
    TextView name;

    //other variables
    String meetingType, meetingRoom;
    String receiverUid, receiverToken, receiverName, receiverAvatar;
    User inviter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outcomming_call);

        //get widgets
        callImageType = findViewById(R.id.callImageType);
        avatar = findViewById(R.id.avatar);
        btn_cancel = findViewById(R.id.btn_cancel);
        name = findViewById(R.id.name);

        //set data
        meetingType = getIntent().getStringExtra("type");
        receiverUid = getIntent().getStringExtra("uid");
        receiverToken = getIntent().getStringExtra("token");
        receiverName = getIntent().getStringExtra("name");
        receiverAvatar = getIntent().getStringExtra("avatar");
        Glide.with(getApplicationContext()).load(receiverAvatar).into(avatar);
        name.setText(receiverName);
        if (meetingType.equals("video")){
            callImageType.setImageResource(R.drawable.video_icon);
        }
        else{
            callImageType.setImageResource(R.drawable.call_icon);
        }

        //set OnClickListener for button cancel
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (receiverToken != null){
                    cancelInvitation(receiverToken);
                }
                onBackPressed();
            }
        });

        inviter = CurrentUserDetails.getInstance().getUser();
        if (meetingType != null && receiverToken != null){
            initiateMeeting(meetingType, receiverToken);
        }
    }

    private void initiateMeeting(String meetingType, String receiverToken){
        try{
            meetingRoom = inviter.id + "_" + receiverUid;

            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            //insert data to data object
            data.put(Constants.KEY_MSG_TYPE, Constants.KEY_MSG_INVITATION);
            data.put(Constants.KEY_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_USER_ID, inviter.id);
            data.put(Constants.KEY_IMAGE, inviter.imageProfile);
            data.put(Constants.KEY_NAME, inviter.name);
            data.put(Constants.KEY_MSG_INVITER_TOKEN, inviter.token);
            data.put(Constants.KEY_MSG_MEETING_ROOM, meetingRoom);

            //insert data to body object
            body.put(Constants.KEY_MSG_DATA, data);
            body.put(Constants.KEY_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.KEY_MSG_INVITATION);
        } catch (Exception ex){
            Toast.makeText(OutcommingCallActivity.this,
                    ex.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()){
                    if (type.equals(Constants.KEY_MSG_INVITATION)){
                        Toast.makeText(OutcommingCallActivity.this,
                                "G???i cu???c g???i th??nh c??ng", Toast.LENGTH_SHORT).show();
                    }
                    else if (type.equals(Constants.KEY_MSG_INVITATION_RESPONSE)){
                        Toast.makeText(OutcommingCallActivity.this,
                                "???? h???y cu???c g???i", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                else{
                    Toast.makeText(OutcommingCallActivity.this,
                            response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(OutcommingCallActivity.this,
                        t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void cancelInvitation(String receiverToken){
        try{
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            //insert data to data object
            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            //insert data to body object
            data.put(Constants.KEY_MSG_TYPE, Constants.KEY_MSG_INVITATION_RESPONSE);
            data.put(Constants.KEY_MSG_INVITATION_RESPONSE, Constants.KEY_MSG_INVITATION_CANCELED);

            body.put(Constants.KEY_MSG_DATA, data);
            body.put(Constants.KEY_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.KEY_MSG_INVITATION_RESPONSE);
        } catch (Exception ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private final BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.KEY_MSG_INVITATION_RESPONSE);
            if (type != null){
                if (type.equals(Constants.KEY_MSG_INVITATION_ACCEPTED)){
                    Toast.makeText(context, "Cu???c g???i ???????c ch???p nh???n", Toast.LENGTH_SHORT).show();

                    //set up Jitsi meeting
                    try {
                        URL serverURL = new URL("https://meet.jit.si");
                        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverURL);
                        builder.setWelcomePageEnabled(false);
                        builder.setRoom(meetingRoom);
                        builder.setVideoMuted(false);
                        if (meetingType.equals("audio")){
                            builder.setVideoMuted(true);
                        }

                        JitsiMeetConferenceOptions conferenceOptions = builder.build();
                        JitsiMeetActivity.launch(OutcommingCallActivity.this, conferenceOptions);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } finally {
                        finish();
                    }
                }
                else if (type.equals(Constants.KEY_MSG_INVITATION_REJECTED)){
                    Toast.makeText(context,"Cu???c g???i b??? t??? ch???i", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.KEY_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }
}
