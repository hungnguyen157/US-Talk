package com.example.ustalk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ViewImageFullSizeActivity extends AppCompatActivity {
    ImageView btnClose, img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image_full_size);

        btnClose = findViewById(R.id.btnClose);
        img = findViewById(R.id.img);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        byte[] bytes_bitmap = intent.getByteArrayExtra("bytes_bitmap");
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes_bitmap,0, bytes_bitmap.length);
        img.setImageBitmap(bitmap);
    }
}