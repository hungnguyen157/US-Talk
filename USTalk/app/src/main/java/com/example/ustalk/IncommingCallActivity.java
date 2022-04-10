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

import org.json.JSONArray;
import org.json.JSONObject;

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
        callImageType = (ImageView) findViewById(R.id.callImageType);
        avatar = (CircleImageView) findViewById(R.id.avatar);
        btn_accept = (ImageView) findViewById(R.id.btn_accept);
        btn_reject = (ImageView) findViewById(R.id.btn_reject);
        name = (TextView) findViewById(R.id.name);

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
        System.out.println("OK2");
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()){
                    if (type.equals(Constants.KEY_MSG_INVITATION_ACCEPTED)){
                        Toast.makeText(IncommingCallActivity.this,
                                "Chấp nhận cuộc gọi", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(IncommingCallActivity.this,
                                "Từ chối cuộc gọi", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    System.out.println("NOT OK");
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
