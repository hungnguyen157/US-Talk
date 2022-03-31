package com.example.ustalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.ustalk.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatHistoryActivity extends Activity implements View.OnClickListener {

    ListView userList;
    ArrayList<User> users = new ArrayList<>();
    CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_user);

        getViewRef();
        loadUsersFromDatabase();

        profileImage.setImageResource(R.drawable.person_icon);
        profileImage.setOnClickListener(this);
    }

    private void getViewRef() {
        userList = (ListView) findViewById(R.id.user_list);
        profileImage = (CircleImageView) findViewById(R.id.profile_image);
    }

    private void loadUsersFromDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = document.toObject(User.class);
                        users.add(user);
                    }
                    userList.setAdapter(new CustomRowAdapter(ChatHistoryActivity.this, R.layout.custom_user_row, users));
                }
                else Log.w("users", "Error getting documents.", task.getException());
            }
        });

//        FirebaseDatabase database = FirebaseDatabase.getInstance("https://us-talk-default-rtdb.asia-southeast1.firebasedatabase.app");
//        DatabaseReference ref = database.getReference("Users");
//
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                users.clear();
//                for(DataSnapshot ds : snapshot.getChildren()) {
////                  String uid = ds.getKey();
//                    User user = ds.getValue(User.class);
//                    users.add(user);
//                }
//                userList.setAdapter(new CustomRowAdapter(ChatHistoryActivity.this, R.layout.custom_user_row, users));
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                System.out.println("Read failed: " + error.getCode());
//            }
//        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.profile_image:
                startActivity(new Intent(ChatHistoryActivity.this, ProfileActivity.class));
                break;
        }
    }
}
