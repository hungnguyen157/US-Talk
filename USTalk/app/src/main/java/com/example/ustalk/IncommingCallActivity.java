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
import com.example.ustalk.network.ApiClient;
import com.example.ustalk.network.ApiService;
import com.example.ustalk.utilities.Constants;

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

public class IncommingCallActivity extends AppCompatActivity {
    //widgets variables
    ImageView callImageType, btn_accept, btn_reject;
    CircleImageView avatar;
    TextView name;

    //other variables
    String meetingType, inviterUid, inviterToken, inviterName, inviterAvatar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomming_call);

        //get widgets
        callImageType = findViewById(R.id.callImageType);
        avatar = findViewById(R.id.avatar);
        btn_accept = findViewById(R.id.btn_accept);
        btn_reject = findViewById(R.id.btn_reject);
        name = findViewById(R.id.name);

        //set data
        meetingType = getIntent().getStringExtra(Constants.KEY_MSG_MEETING_TYPE);
        inviterUid = getIntent().getStringExtra(Constants.KEY_USER_ID);
        inviterName = getIntent().getStringExtra(Constants.KEY_NAME);
        inviterAvatar = getIntent().getStringExtra(Constants.KEY_IMAGE);
        inviterToken = getIntent().getStringExtra(Constants.KEY_MSG_INVITER_TOKEN);
        if (meetingType.equals("video")){
            callImageType.setImageResource(R.drawable.video_icon);
        }
        else{
            callImageType.setImageResource(R.drawable.call_icon);
        }
        Glide.with(getApplicationContext()).load(inviterAvatar).into(avatar);
        name.setText(inviterName);

        //set OnClickListener for accept and reject button
        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendInvitationResponse(Constants.KEY_MSG_INVITATION_ACCEPTED, inviterToken);
            }
        });
        btn_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendInvitationResponse(Constants.KEY_MSG_INVITATION_REJECTED, inviterToken);
            }
        });
    }

    private void sendInvitationResponse(String type, String inviterToken){
        try{
            JSONArray tokens = new JSONArray();
            tokens.put(inviterToken);

            //insert data to data object
            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            //insert data to body object
            data.put(Constants.KEY_MSG_TYPE, Constants.KEY_MSG_INVITATION_RESPONSE);
            data.put(Constants.KEY_MSG_INVITATION_RESPONSE, type);

            body.put(Constants.KEY_MSG_DATA, data);
            body.put(Constants.KEY_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), type);
        } catch (Exception ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
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
                    if (type.equals(Constants.KEY_MSG_INVITATION_ACCEPTED)){
                        Toast.makeText(IncommingCallActivity.this,
                                "Ch???p nh???n cu???c g???i", Toast.LENGTH_SHORT).show();

                        //set up Jitsi meeting
                        try {
                            URL serverURL = new URL("https://meet.jit.si");
                            JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                            builder.setServerURL(serverURL);
                            builder.setWelcomePageEnabled(false);
                            builder.setRoom(getIntent().getStringExtra(Constants.KEY_MSG_MEETING_ROOM));
                            builder.setVideoMuted(false);
                            if (meetingType.equals("audio")){
                                builder.setVideoMuted(true);
                            }

                            JitsiMeetConferenceOptions conferenceOptions = builder.build();
                            JitsiMeetActivity.launch(IncommingCallActivity.this, conferenceOptions);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } finally {
                            finish();
                        }
                    }
                    else{
                        Toast.makeText(IncommingCallActivity.this,
                                "T??? ch???i cu???c g???i", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(IncommingCallActivity.this,
                            response.message(), Toast.LENGTH_SHORT).show();
                }
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(IncommingCallActivity.this,
                        t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private final BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.KEY_MSG_INVITATION_RESPONSE);
            if (type != null){
                if (type.equals(Constants.KEY_MSG_INVITATION_CANCELED)){
                    Toast.makeText(context,"Cu???c g???i b??? hu???", Toast.LENGTH_SHORT).show();
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
