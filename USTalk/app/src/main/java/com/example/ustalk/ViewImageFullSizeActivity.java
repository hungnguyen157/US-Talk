package com.example.ustalk;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ViewImageFullSizeActivity extends AppCompatActivity {
    //widget variables
    ImageView btnClose, img, btnSave;

    //other variables
    private boolean isReadPermissionGranted = false;
    private boolean isWritePermissionGranted = false;
    ActivityResultLauncher<String[]> mPermissionResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image_full_size);

        //get widgets
        btnClose = findViewById(R.id.btnClose);
        img = findViewById(R.id.img);
        btnSave = findViewById(R.id.btnSave);

        //set data to image
        Intent intent = getIntent();
        byte[] bytes_bitmap = intent.getByteArrayExtra("bytes_bitmap");
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes_bitmap,0, bytes_bitmap.length);
        img.setImageBitmap(bitmap);

        //set activity and listener
        mPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                if (result.get(Manifest.permission.READ_EXTERNAL_STORAGE) != null){
                    isReadPermissionGranted = result.get(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
                if (result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) != null){
                    isReadPermissionGranted = result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission();
                if (isWritePermissionGranted){
                    String imageName = "" + System.currentTimeMillis();
                    saveImageToExternalStorage(imageName, bitmap);
                }
                else{
                    Toast.makeText(ViewImageFullSizeActivity.this,
                            "Hãy cho phép ứng dụng truy cập vào thư viện ảnh của thiết bị " +
                                    "để thực hiện tác vụ này", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void requestPermission(){
        boolean minSDK = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

        //check the permission of reading external storage
        isReadPermissionGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        //check the permission of reading external storage for SDK 28 and below
        isWritePermissionGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        isWritePermissionGranted = isWritePermissionGranted || minSDK;

        List<String> permissionRequest = new ArrayList<>();

        if (!isReadPermissionGranted){
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!isWritePermissionGranted){
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionRequest.isEmpty()){
            mPermissionResultLauncher.launch(permissionRequest.toArray(new String[0]));
        }
    }

    private void saveImageToExternalStorage(String imageName, Bitmap bitmap){
        Uri imageCollection;
        ContentResolver resolver = getContentResolver();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }
        else{
            imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imageName + "png");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        Uri imageUri = resolver.insert(imageCollection, contentValues);

        try{
            //save the image
            OutputStream outputStream = resolver.openOutputStream(Objects.requireNonNull(imageUri));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            Objects.requireNonNull(outputStream);
            Toast.makeText(this, "Lưu ảnh thành công", Toast.LENGTH_SHORT).show();

            //clear outputStream
            outputStream.flush();
            outputStream.close();
        } catch (Exception ex){
            Toast.makeText(this, "Lưu ảnh không thành công", Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }
}