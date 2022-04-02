package com.example.ustalk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.ustalk.models.User;
import com.example.ustalk.utilities.CurrentUserDetails;
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

import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatHistoryActivity extends Activity implements View.OnClickListener {

    ListView userList;
    ArrayList<User> users = new ArrayList<>();
    CircleImageView profileImage;
    private ArrayList<String> uids = new ArrayList<>();

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
                        uids.add(document.getId());
                        users.add(user);
                    }
                    userList.setAdapter(new CustomRowAdapter(ChatHistoryActivity.this, R.layout.custom_user_row, users));
                    userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            String myUid = CurrentUserDetails.getInstance().getUid();
                            String theirUid = uids.get(i);
                            String memberIds[] = new String[]{myUid, theirUid};
                            Arrays.sort(memberIds);
                            String roomID = String.join(" ", memberIds);
                            db.collection("rooms").document(roomID).set(new ChatRoom(memberIds));

                            Intent intent = new Intent(ChatHistoryActivity.this, ChatActivity.class);
                            intent.putExtra("ids", theirUid);
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
                startActivity(new Intent(ChatHistoryActivity.this, SettingActivity.class));
                break;
        }
    }
}
