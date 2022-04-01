package com.example.ustalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class SettingActivity extends Activity implements View.OnClickListener {
    LinearLayout Profile;
    LinearLayout Password;
    LinearLayout Notifiation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Profile = (LinearLayout) findViewById(R.id.profile_navigate);
        Password = (LinearLayout) findViewById(R.id.password_navigate);
        Notifiation = (LinearLayout) findViewById(R.id.notification_navigate);
        Profile.setOnClickListener(this);
        Password.setOnClickListener(this);
        Notifiation.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == Profile.getId())
        {
            startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
        }
        else if(view.getId() == Password.getId())
        {

        }
        else if(view.getId() == Notifiation.getId())
        {

        }
    }
}
