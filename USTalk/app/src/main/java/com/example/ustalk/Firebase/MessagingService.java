package com.example.ustalk.Firebase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.ustalk.R;
import com.example.ustalk.utilities.PreferenceManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MessagingService extends FirebaseMessagingService {
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
        Log.d("FCM","Notification: "+remoteMessage.getNotification().getBody());
        Log.d("FCM","From: "+remoteMessage.getFrom());
        Log.d("FCM","Data: "+remoteMessage.getData());

        Map<String, String> data = remoteMessage.getData();
        String name = data.get("name");
        String message = data.get("message");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "chatMessage");
        builder.setSmallIcon(R.drawable.ic_baseline_notifications_24);
        builder.setContentTitle(name);
        builder.setContentText(message);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setAutoCancel(true);

        //if sdk...
        //...

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(new Random().nextInt(),builder.build());
    }
}
