package com.example.ustalk;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.ustalk.models.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomRowAdapter extends ArrayAdapter<User> {
    Context context;
    String[] userName,lastMsg;
    Integer[] avatars;
    ArrayList<User> users;

    CustomRowAdapter(Context context, int layoutToBeInflated, ArrayList<User> users){
        super(context, layoutToBeInflated, users);
        this.context = context;
        this.users = users;
//        this.userName = userName;
//        this.avatars = avatars;
//        this.lastMsg = lastMsg;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(R.layout.custom_user_row, null);
        TextView txtUserName = (TextView) row.findViewById(R.id.userName);
        CircleImageView avatar = (CircleImageView) row.findViewById(R.id.profile_image);
        TextView txtLastMsg = (TextView) row.findViewById(R.id.lastMsg);

        txtUserName.setText(users.get(position).name);
        avatar.setImageResource(R.drawable.person_icon);
        txtLastMsg.setText("Deez nuts");
//        if (position == selected_pos){
//            row.setBackground(context.getResources().
//                    getDrawable(R.drawable.custom_list_selector, context.getTheme()));
//        }
//        else{
//            row.setBackgroundColor(Color.TRANSPARENT);
//        }

        return (row);
    }
}
