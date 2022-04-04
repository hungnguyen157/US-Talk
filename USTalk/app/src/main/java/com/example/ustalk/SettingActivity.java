package com.example.ustalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.ustalk.utilities.CurrentUserDetails;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends Activity implements View.OnClickListener {
    LinearLayout Profile;
    LinearLayout Password;
    LinearLayout Notifiation;
    ImageView btnBack;
    FirebaseFirestore db;
    TextView txtName;
    CircleImageView profileImage;
    ImageView btn_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        db = FirebaseFirestore.getInstance();
        Profile = (LinearLayout) findViewById(R.id.profile_navigate);
        Password = (LinearLayout) findViewById(R.id.password_navigate);
        Notifiation = (LinearLayout) findViewById(R.id.notification_navigate);
        btnBack =(ImageView) findViewById(R.id.btn_back);
        txtName = (TextView) findViewById(R.id.name);
        profileImage = (CircleImageView) findViewById(R.id.profile_image);
        Profile.setOnClickListener(this);
        Password.setOnClickListener(this);
        Notifiation.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        showInfo();
    }
    private void showInfo(){
        db.collection("users").document(CurrentUserDetails.getInstance().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String name = documentSnapshot.getString("name");
                        String imageProfile = documentSnapshot.getString("imageProfile");
                        txtName.setText(name);
                        Glide.with(SettingActivity.this).load(imageProfile).into(profileImage);
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == Profile.getId())
        {
            startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
        }
        else if(view.getId() == Password.getId())
        {
            startActivity(new Intent(getApplicationContext(),ChangePasswordActivity.class));
        }
        else if(view.getId() == Notifiation.getId())
        {

        }
        else if (view.getId()==btnBack.getId())
            onBackPressed();
    }
}
