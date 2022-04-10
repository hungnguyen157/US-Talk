package com.example.ustalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.ustalk.utilities.CurrentUserDetails;
import com.example.ustalk.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends OnlineActivity implements View.OnClickListener {
    LinearLayout Profile;
    LinearLayout Password;
    LinearLayout Notifiation;
    ImageView btnBack;
    FirebaseFirestore db;
    TextView txtName;
    CircleImageView profileImage;
    ImageView btn_back;
    Button btnSignOut;
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
        btnSignOut = findViewById(R.id.btnSignout);
        Profile.setOnClickListener(this);
        Password.setOnClickListener(this);
        Notifiation.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);

        showInfo();
    }
    private void showInfo(){
        DocumentReference documentReference = db.collection("users").document(CurrentUserDetails.getInstance().getUid());
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error!=null){
                    System.err.println("Listen failed: " + error);
                }
                if (value!= null && value.exists()) {
                    String name = value.getString("name");
                    String imageProfile = value.getString("imageProfile");
                    txtName.setText(name);
                    Glide.with(getApplicationContext()).load(imageProfile).into(profileImage);
                } else {
                    System.out.print("Current data: null");
                }
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
        else if(view.getId() == btnSignOut.getId())
        {
            PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
            String uid = preferenceManager.getString("UID");
            HashMap<String, Object> changes = new HashMap<>();
            changes.put("online", false);
            changes.put("token", null);
            db.collection("users").document(uid).update(changes)
                    .addOnFailureListener(e -> Log.e("online", e.getMessage()));
            preferenceManager.remove("UID");

            Toast.makeText(getApplicationContext(), "Bạn đã đăng xuất", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(),SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        else if (view.getId()==btnBack.getId()) {
            onBackPressed();
        }
    }
}
