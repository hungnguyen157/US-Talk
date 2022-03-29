package com.example.ustalk;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatHistoryActivity extends Activity {

    ListView userList;
    ArrayList<User> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_user);

        loadUsersFromDatabase();

        userList = (ListView) findViewById(R.id.user_list);

    }

    private void loadUsersFromDatabase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://us-talk-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference ref = database.getReference("Users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot ds : snapshot.getChildren()) {
//                  String uid = ds.getKey();
                    User user = ds.getValue(User.class);
                    users.add(user);
                }
                userList.setAdapter(new CustomRowAdapter(ChatHistoryActivity.this, R.layout.custom_user_row, users));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Read failed: " + error.getCode());
            }
        });
    }
}
