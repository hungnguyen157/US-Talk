package com.example.ustalk;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class ProfileActivity extends Activity implements View.OnClickListener {
//    ImageButton btnBack;
//    Button btnEdit;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_profile);
//        btnBack = (ImageButton) findViewById(R.id.btnBack);
//        btnEdit = (Button) findViewById(R.id.btnEdit);
//        btnEdit.setOnClickListener(this);
//        btnBack.setOnClickListener(this);
//    }
//
//    @Override
//    public void onClick(View view) {
//        switch(view.getId()){
//            case R.id.btnEdit:{
//                startActivity(new Intent(getApplicationContext(), EditProfile.class));
//                break;
//            }
//        }
//    }
//widget variables
    ImageView btnBack, profile_image;
    FloatingActionButton fab;
    EditText editName;
    RadioGroup sex_field;
    RadioButton rbtn_male, rbtn_female;
    Button btnEdit, btnSave, btnCancel;
    private BottomSheetDialog bottomsheet;
    private int IMAGE_GALLERY_REQUEST = 3;
    //data variables
    String current_name = "Nguyễn Quốc Thông";
    int current_sex = R.id.rbtn_male;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //get widgets
        btnBack = (ImageView) findViewById(R.id.btnBack);
        profile_image = (ImageView) findViewById(R.id.profile_image);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        editName = (EditText) findViewById(R.id.editName);
        sex_field = (RadioGroup) findViewById(R.id.sex_field);
        rbtn_male = (RadioButton) findViewById(R.id.rbtn_male);
        rbtn_female = (RadioButton) findViewById(R.id.rbtn_female);
        btnEdit = (Button) findViewById(R.id.btnEdit);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        //set data for sex_field
        if (current_sex == rbtn_male.getId()){
            rbtn_male.setChecked(true);
        }
        else{
            rbtn_female.setChecked(true);
        }

        //disable EditText and RadioGroup
        editName.setEnabled(false);
        rbtn_male.setEnabled(false);
        rbtn_female.setEnabled(false);

        //set invisible for "Save" and "Cancel" button
        btnSave.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);

        //set OnClickListener for buttons
        btnBack.setOnClickListener(this);
        fab.setOnClickListener(this);
        btnEdit.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case (R.id.btnBack):{
                onBackPressed();
                break;
            }
            case (R.id.fab):{
                //choose and change profile image
                openGallery();
                break;
            }
            case (R.id.btnEdit):{
                editName.setEnabled(true);
                editName.requestFocus(current_name.length()-1);
                rbtn_male.setEnabled(true);
                rbtn_male.setButtonTintList(ColorStateList.valueOf(getColor(R.color.orange)));
                rbtn_female.setEnabled(true);
                rbtn_female.setButtonTintList(ColorStateList.valueOf(getColor(R.color.orange)));
                btnEdit.setVisibility(View.GONE);
                btnSave.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                break;
            }
            case (R.id.btnSave):{
                String new_name = editName.getText().toString();
                int new_sex = sex_field.getCheckedRadioButtonId();
                if (!new_name.equals(current_name) || new_sex != current_sex){
                    //do the save work
                }
            }
            case (R.id.btnCancel):{
                editName.setEnabled(false);
                rbtn_male.setEnabled(false);
                rbtn_male.setButtonTintList(ColorStateList.valueOf(getColor(R.color.dark_gray)));
                rbtn_female.setEnabled(false);
                rbtn_female.setButtonTintList(ColorStateList.valueOf(getColor(R.color.dark_gray)));
                btnEdit.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                break;
            }
        }
    }

    private void openGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_GALLERY_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK&&data!=null){
            ImageView profile_image = (ImageView) findViewById(R.id.profile_image);
            Uri imageUri = data.getData();
            profile_image.setImageURI(imageUri);
        }
    }
}