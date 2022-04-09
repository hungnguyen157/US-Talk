package com.example.ustalk;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

public class OutcommingCallActivity extends AppCompatActivity {
    ImageView callImageType, btn_cancel;
    CircleImageView avatar;
    TextView name;
    String type, receiverUid, receiveruidToken, receiverName, receiverAvatar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outcomming_call);

        //get widgets
        callImageType = (ImageView) findViewById(R.id.callImageType);
        avatar = (CircleImageView) findViewById(R.id.avatar);
        btn_cancel = (ImageView) findViewById(R.id.btn_cancel);
        name = (TextView) findViewById(R.id.name);

        //set data
        type = getIntent().getStringExtra("type");
        receiverUid = getIntent().getStringExtra("uid");
        receiveruidToken = getIntent().getStringExtra("token");
        receiverName = getIntent().getStringExtra("name");
        receiverAvatar = getIntent().getStringExtra("avatar");
        Glide.with(getApplicationContext()).load(receiverAvatar).into(avatar);
        name.setText(getIntent().getStringExtra(receiverName));
        if (type.equals("video")){
            callImageType.setImageResource(R.drawable.video_icon);
        }
        else{
            callImageType.setImageResource(R.drawable.call_icon);
        }

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}
