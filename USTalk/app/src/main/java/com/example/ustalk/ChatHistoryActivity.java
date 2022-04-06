package com.example.ustalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.ustalk.models.User;
import com.example.ustalk.utilities.CurrentUserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatHistoryActivity extends Activity implements View.OnClickListener {

    ListView userList;
    ArrayList<User> users = new ArrayList<>();
    CircleImageView profileImage;
    private ArrayList<String> uids = new ArrayList<>();
    private ArrayList<String> name = new ArrayList<>();
    private ArrayList<String> image = new ArrayList<>();
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_user);

        db = FirebaseFirestore.getInstance();
        getViewRef();
        loadUsersFromDatabase();
        showInfo();
        profileImage.setOnClickListener(this);
    }

    private void getViewRef() {
        userList = (ListView) findViewById(R.id.user_list);
        profileImage = (CircleImageView) findViewById(R.id.profile_image);
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
                    String imageProfile = value.getString("imageProfile");
                    Glide.with(ChatHistoryActivity.this).load(imageProfile).into(profileImage);
                } else {
                    System.out.print("Current data: null");
                }
            }
        });
    }

    private void loadUsersFromDatabase() {
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = document.toObject(User.class);
                        uids.add(document.getId());
                        name.add(user.name);
                        if(user.imageProfile == null)
                        {
                            user.setImage("https://firebasestorage.googleapis.com/v0/b/us-talk.appspot.com/o/Avatar%2F164905463799677ci49SJ4JOzmqC7lzPwVW9Axh42?alt=media&token=6e779a68-2e10-414b-b8e6-ff6d2851f34b");
                            image.add("https://firebasestorage.googleapis.com/v0/b/us-talk.appspot.com/o/Avatar%2F164905463799677ci49SJ4JOzmqC7lzPwVW9Axh42?alt=media&token=6e779a68-2e10-414b-b8e6-ff6d2851f34b");
                        }
                        else
                        {
                            image.add(user.imageProfile);
                        }
                        users.add(user);
                    }
                    userList.setAdapter(new UserAdapter(ChatHistoryActivity.this, R.layout.custom_user_row, users));
                    userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            String myUid = CurrentUserDetails.getInstance().getUid();
                            String theirUid = uids.get(i);
                            String image1 = image.get(i);
                            String name1 = name.get(i);
                            Intent intent = new Intent(ChatHistoryActivity.this, ChatActivity.class);
                            intent.putExtra("imageProfile", image1);
                            intent.putExtra("name",name1);
                            intent.putExtra("receiveID",theirUid);
                            startActivity(intent);
                        }
                    });
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
//                userList.setAdapter(new UserAdapter(ChatHistoryActivity.this, R.layout.custom_user_row, users));
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
                startActivity(new Intent(ChatHistoryActivity.this, SettingActivity.class));
                break;
        }
    }
}
