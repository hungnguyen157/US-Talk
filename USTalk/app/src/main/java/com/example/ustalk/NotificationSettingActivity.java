package com.example.ustalk;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.ustalk.utilities.PreferenceManager;

import java.time.Instant;

public class NotificationSettingActivity extends OnlineActivity implements View.OnClickListener {

    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_setting);

        preferenceManager = new PreferenceManager(getApplicationContext());
        findViewById(R.id.btnOn).setOnClickListener(this);
        findViewById(R.id.btn15min).setOnClickListener(this);
        findViewById(R.id.btn30min).setOnClickListener(this);
        findViewById(R.id.btn60min).setOnClickListener(this);
        findViewById(R.id.btnOff).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnOn:
                preferenceManager.remove("whenNotificationOff");
                preferenceManager.remove("durationNotificationOff");
                break;
            case R.id.btn15min:
                turnOffNotification(15);
                break;
            case R.id.btn30min:
                turnOffNotification(30);
                break;
            case R.id.btn60min:
                turnOffNotification(60);
                break;
            case R.id.btnOff:
                turnOffNotification(-1);
                break;
        }
    }

    private void turnOffNotification(int minutes) {
        Instant now = Instant.now();
        preferenceManager.putString("whenNotificationOff", now.toString());
        preferenceManager.putString("durationNotificationOff", String.valueOf(minutes));
    }
}