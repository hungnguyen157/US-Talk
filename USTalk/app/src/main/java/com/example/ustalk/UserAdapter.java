package com.example.ustalk;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.ustalk.models.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends ArrayAdapter<User> {
    private final ArrayList<String> uids;
    private final ArrayList<String> lastMessages;
    Context context;
    ArrayList<User> users;

    UserAdapter(Context context, int layoutToBeInflated, ArrayList<User> users, ArrayList<String> uids, ArrayList<String> lastMessages){
        super(context, layoutToBeInflated, users);
        this.context = context;
        this.users = users;
        this.uids = uids;
        this.lastMessages = lastMessages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i("counter", "get view" + position);

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(R.layout.custom_user_row, null);
        TextView txtUserName = (TextView) row.findViewById(R.id.userName);
        CircleImageView avatar = (CircleImageView) row.findViewById(R.id.profile_image);
        TextView txtLastMsg = (TextView) row.findViewById(R.id.lastMsg);

        txtUserName.setText(users.get(position).name);
        Glide.with(getContext()).load(users.get(position).imageProfile).into(avatar);
        String lastMessage = lastMessages.get(position);
        if (lastMessage == null) lastMessage = "No message yet";
        txtLastMsg.setText(lastMessage);
        return (row);
    }
}
