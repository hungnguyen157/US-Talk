package com.example.ustalk;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;

import com.example.ustalk.utilities.PreferenceManager;

import java.time.Instant;

public class NotificationSettingActivity extends OnlineActivity implements View.OnClickListener {
    //widget variables
    ImageView btnBack;
    SwitchCompat switchOnOffNotification;

    //other variables
    PreferenceManager preferenceManager;
    Dialog dialog;
    private boolean previousState = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_setting);

        preferenceManager = new PreferenceManager(getApplicationContext());

        //get widgets
        btnBack = findViewById(R.id.btnBack);
        switchOnOffNotification = findViewById(R.id.switchOnOffNotification);
        switchOnOffNotification.setChecked(true);

        //set listener for widgets
        btnBack.setOnClickListener(this);
        switchOnOffNotification.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b){
                if (previousState != b) {
//                        preferenceManager.remove("whenNotificationOff");
//                        preferenceManager.remove("durationNotificationOff");
                    Toast.makeText(getApplicationContext(), "On", Toast.LENGTH_SHORT).show();
                    previousState = b;
                }
            }
            else {
                showTurnOffNotificationDialog();
            }
        });
    }

    @Override
    public void onClick(View view) {
        int minutes = 0;
        switch (view.getId()) {
            case (R.id.btnBack): {
                onBackPressed();
            }
            case (R.id.txt15Minutes): {
                minutes = 15;
                break;
            }
            case (R.id.txt30Minutes): {
                minutes = 30;
                break;
            }
            case (R.id.txt60Minutes): {
                minutes = 60;
                break;
            }
            case (R.id.txtUntilITurnOn): {
                minutes = -1;
                break;
            }
            case (R.id.txtCancel): {
                minutes = 0;
                break;
            }
        }
        if (minutes != 0) {
            turnOffNotification(minutes);
            previousState = false;
        }
        else {
            switchOnOffNotification.setChecked(true);
        }
        dialog.dismiss();
    }

    private void turnOffNotification(int minutes) {
        Instant now = Instant.now();
//        preferenceManager.putString("whenNotificationOff", now.toString());
//        preferenceManager.putString("durationNotificationOff", String.valueOf(minutes));
        Toast.makeText(getApplicationContext(), "Off" + minutes, Toast.LENGTH_SHORT).show();
    }

    private void showTurnOffNotificationDialog() {
        dialog =  new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.turn_off_notification_options_layout);
        dialog.setCanceledOnTouchOutside(false);

        TextView txt15Minutes, txt30Minutes, txt60Minutes, txtUntilITurnOn, txtCancel;
        txt15Minutes = dialog.findViewById(R.id.txt15Minutes);
        txt30Minutes = dialog.findViewById(R.id.txt30Minutes);
        txt60Minutes = dialog.findViewById(R.id.txt60Minutes);
        txtUntilITurnOn = dialog.findViewById(R.id.txtUntilITurnOn);
        txtCancel = dialog.findViewById(R.id.txtCancel);

        txt15Minutes.setOnClickListener(this);
        txt30Minutes.setOnClickListener(this);
        txt60Minutes.setOnClickListener(this);
        txtUntilITurnOn.setOnClickListener(this);
        txtCancel.setOnClickListener(this);

        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
    }
}