package com.example.ustalk.Firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.ustalk.ChatActivity;
import com.example.ustalk.IncommingCallActivity;
import com.example.ustalk.R;
import com.example.ustalk.models.User;
import com.example.ustalk.utilities.Constants;
import com.example.ustalk.utilities.CurrentUserDetails;
import com.example.ustalk.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MessagingService extends FirebaseMessagingService {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onNewToken(@NonNull String token)
    {
        super.onNewToken(token);
        Log.d("FCM","Token" + token);
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        preferenceManager.putString("token", token);
    }
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage)
    {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        String uid = data.get("uid");
        db.document("users/" + uid).get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            String name = data.get("name");
            String message = data.get("message");

            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("name", name);
            intent.putExtra("imageProfile", user.imageProfile);
            intent.putExtra("receiveID", uid);
            intent.putExtra("token", user.token);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

            String channelId = "chatMessage";
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
            builder.setSmallIcon(R.drawable.ic_baseline_notifications_24);
            builder.setContentTitle(name);
            builder.setContentText(message);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence channelName = "USTalk";
                String channelDescription = "channel of USTalk";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
                channel.setDescription(channelDescription);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
            notificationManagerCompat.notify(new Random().nextInt(),builder.build());
        })
            .addOnFailureListener(e -> Log.e("notification", e.getMessage()));

        String type = data.get(Constants.KEY_MSG_TYPE);
        if (type != null){
            if (type.equals(Constants.KEY_MSG_INVITATION)){
                Intent intent = new Intent(getApplicationContext(), IncommingCallActivity.class);
                intent.putExtra(Constants.KEY_MSG_MEETING_TYPE,
                                data.get(Constants.KEY_MSG_MEETING_TYPE)
                );
                intent.putExtra(Constants.KEY_USER_ID,
                                data.get(Constants.KEY_USER_ID)
                );
                intent.putExtra(Constants.KEY_IMAGE,
                                data.get(Constants.KEY_IMAGE)
                );
                intent.putExtra(Constants.KEY_NAME,
                                data.get(Constants.KEY_NAME)
                );
                intent.putExtra(Constants.KEY_MSG_INVITER_TOKEN,
                                data.get(Constants.KEY_MSG_INVITER_TOKEN)
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            else if (type.equals(Constants.KEY_MSG_INVITATION_RESPONSE)){
                Intent intent = new Intent(Constants.KEY_MSG_INVITATION_RESPONSE);
                intent.putExtra(Constants.KEY_MSG_INVITATION_RESPONSE,
                                data.get(Constants.KEY_MSG_INVITATION_RESPONSE));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }
    }
}
