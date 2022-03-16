package com.example.ustalk;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomRowAdapter extends ArrayAdapter<String> {
    Context context;
    String[] userName,lastMsg;
    Integer[] avatars;

    CustomRowAdapter(Context context, int layoutToBeInflated, String[] userName, Integer[] avatars,String []lastMsg){
        super(context, layoutToBeInflated, userName);
        this.context = context;
        this.userName = userName;
        this.avatars = avatars;
        this.lastMsg = lastMsg;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(R.layout.custom_user_row, null);
        TextView txtUserName = (TextView) row.findViewById(R.id.userName);
        CircleImageView avatar = (CircleImageView) row.findViewById(R.id.profile_image);
        TextView txtLastMsg = (TextView) row.findViewById(R.id.lastMsg);

        txtUserName.setText(userName[position]);
        avatar.setImageResource(avatars[position]);
        txtLastMsg.setText(lastMsg[position]);
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
