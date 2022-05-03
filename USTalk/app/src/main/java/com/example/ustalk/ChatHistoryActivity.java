package com.example.ustalk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.ustalk.models.ChatMessage;
import com.example.ustalk.models.User;
import com.example.ustalk.utilities.CurrentUserDetails;
import com.example.ustalk.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatHistoryActivity extends OnlineActivity implements View.OnClickListener {

    ListView userList;
    ArrayList<User> users = new ArrayList<>();
    CircleImageView profileImage;
    private final ArrayList<String> uids = new ArrayList<>();
    private final ArrayList<Date> lastTimes = new ArrayList<>();
    private final ArrayList<String> lastMessages = new ArrayList<>();
    FirebaseFirestore db;
    PreferenceManager prefManager;
    private final int REQUEST_CODE_BATTERY_OPTIMIZATION = 1;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_user);

        db = FirebaseFirestore.getInstance();
        prefManager = new PreferenceManager(getApplicationContext());
        getViewRef();
        loadUsersFromDatabase();
        loadCurrentUserFromDatabase();
        profileImage.setOnClickListener(this);

        userAdapter = new UserAdapter(ChatHistoryActivity.this, R.layout.custom_user_row, users, uids, lastMessages);
        userList.setAdapter(userAdapter);
        userList.setOnItemClickListener((adapterView, view, i, l) -> {
            String theirUid = uids.get(i);
            Intent intent = new Intent(ChatHistoryActivity.this, ChatActivity.class);
            intent.putExtra("receiveID",theirUid);
            startActivity(intent);
        });

        checkForBatteryOptimizations();
    }

    private void loadCurrentUserFromDatabase() {
        String uid = prefManager.getString("UID");
        DocumentReference ref = db.collection("users").document(uid);
        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = task.getResult().toObject(User.class);
                updateUserToken(user, uid);
                CurrentUserDetails storage = CurrentUserDetails.getInstance();
                storage.setUser(user);
                storage.setUid(uid);
                showInfo();
            }
            else Log.w("signin", "Error getting document.", task.getException());
        });
        ref.update("online", true).addOnFailureListener(e -> Log.e("online", e.getMessage()));
    }

    private void updateUserToken(User user, String uid) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(newToken -> {
                    if (!newToken.equals(user.token)) {
                        db.collection("users").document(uid).update("token", newToken)
                                .addOnFailureListener(e -> Log.e("token", e.getMessage()));
                        user.token = newToken;
                    }
                })
                .addOnFailureListener(e -> Log.e("updateToken", e.getMessage()));
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
                    Glide.with(getApplicationContext()).load(imageProfile).into(profileImage);
                } else {
                    System.out.print("Current data: null");
                }
            }
        });
    }

    private void loadUsersFromDatabase() {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i("counter", "load users from db");
                String myUid = prefManager.getString("UID");

                int i = 0;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    User user = document.toObject(User.class);
                    String theirUid = document.getId();
                    uids.add(theirUid);
                    users.add(user);
                    lastTimes.add(null);
                    lastMessages.add(null);

                    int finalI = i;
                    EventListener<QuerySnapshot> listener = (value, error) -> {
                        if (error != null) {
                            Log.e("last message", error.getMessage());
                            return;
                        }
                        if (value == null) {
                            Log.e("last message", "value is null");
                            return;
                        }
                        for (DocumentChange doc : value.getDocumentChanges()) {
                            ChatMessage message = ChatMessage.fromDocumentChange(doc);
                            Date time = message.time;
                            Date recentTime = lastTimes.get(finalI);
                            if (recentTime == null || time.after(recentTime)) {
                                lastTimes.set(finalI, time);
                                String lastMessage = "";
                                if (message.senderID.equals(myUid)) lastMessage = "Bạn: ";
                                if (!message.sendimage && !message.sendvoice) lastMessage += message.message;
                                if (message.sendimage) lastMessage += "Đã gửi một hình ảnh";
                                else if (message.sendvoice) lastMessage += "Đã gửi một tin nhắn âm thanh";
                                lastMessages.set(finalI, lastMessage);
                                userAdapter.notifyDataSetChanged();
                            }
                        }
                    };
                    db.collection("chat")
                            .whereEqualTo("senderID", myUid)
                            .whereEqualTo("RecceiveID", theirUid)
                            .addSnapshotListener(listener);
                    db.collection("chat")
                            .whereEqualTo("senderID", theirUid)
                            .whereEqualTo("RecceiveID", myUid)
                            .addSnapshotListener(listener);
                    i++;
                }
                userAdapter.notifyDataSetChanged();
            }
            else Log.w("users", "Error getting documents.", task.getException());
        });
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

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    private void checkForBatteryOptimizations(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())){
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatHistoryActivity.this);
                builder.setTitle("Warning");
                builder.setMessage("Battery optimization is enabled. It can interrupt running background service");
                builder.setPositiveButton("Disable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATION);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BATTERY_OPTIMIZATION){
            checkForBatteryOptimizations();
        }
    }
}
