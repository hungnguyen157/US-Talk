package com.example.ustalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ustalk.databinding.ActivityProfileBinding;
import com.example.ustalk.models.User;
import com.example.ustalk.utilities.CurrentUserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
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
    TextView editEmail;
    private int IMAGE_GALLERY_REQUEST = 3;
    private Uri imageUri;
    FirebaseFirestore fireStore;
    ProgressDialog progressDialog;
    int flag =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        fireStore = FirebaseFirestore.getInstance();
        //get widgets
        editEmail = (TextView) findViewById(R.id.editEmail);
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
        showInfo();
        progressDialog = new ProgressDialog(this);

        //disable EditText and RadioGroup
        editName.setEnabled(false);
        rbtn_male.setEnabled(false);
        rbtn_female.setEnabled(false);

        //set invisible for "Save" and "Cancel" button
        btnSave.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);
        fab.setVisibility(View.INVISIBLE);

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
                editName.requestFocus(editName.getText().toString().length()-1);
                rbtn_male.setEnabled(true);
                rbtn_male.setButtonTintList(ColorStateList.valueOf(getColor(R.color.blue_purple)));
                rbtn_female.setEnabled(true);
                rbtn_female.setButtonTintList(ColorStateList.valueOf(getColor(R.color.blue_purple)));
                btnEdit.setVisibility(View.GONE);
                btnSave.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);
                break;
            }
            case (R.id.btnSave):{
                uploadToFirebase();
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
                fab.setVisibility(View.GONE);
                flag = 0;
                showInfo();
                break;
            }
        }
    }
    private void showInfo(){
        fireStore.collection("users").document(CurrentUserDetails.getInstance().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            String email = documentSnapshot.getString("email");
                            String userName = documentSnapshot.getString("name");
                            String sex = documentSnapshot.getString("sex");
                            String imageProfile = documentSnapshot.getString("imageProfile");
                            editName.setText(userName);
                            editEmail.setText(email);
                            if(sex.equals("Nam"))
                                rbtn_male.setChecked(true);
                            else
                                rbtn_female.setChecked(true);
                            Glide.with(ProfileActivity.this).load(imageProfile).into(profile_image);
                    }
                }
    });
}

    private void openGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,IMAGE_GALLERY_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK&&data!=null){
            imageUri = data.getData();
            profile_image.setImageURI(imageUri);
            flag =1;
        }
    }
    private void uploadToFirebase(){
        int selectedID = sex_field.getCheckedRadioButtonId();
        RadioButton selectRadio = (RadioButton) findViewById(selectedID);
        HashMap<String,Object> hashmapName =new HashMap<>();
        hashmapName.put("name",editName.getText().toString());
        hashmapName.put("sex",selectRadio.getText().toString());
        fireStore.collection("users").document(CurrentUserDetails.getInstance().getUid()).update(hashmapName);
        if(imageUri!=null&&flag==1){
            progressDialog.setMessage("Đang cập nhật...");
            progressDialog.show();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference mountainsRef = storageRef.child("Avatar/"+System.currentTimeMillis()+CurrentUserDetails.getInstance().getUid());
            mountainsRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task <Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!urlTask.isSuccessful());
                    Uri downloadUrl = urlTask.getResult();
                    String url = downloadUrl.toString();
                    HashMap<String, Object> hashmap =new HashMap<>();
                    hashmap.put("imageProfile",url);
                    fireStore.collection("users").document(CurrentUserDetails.getInstance().getUid()).update(hashmap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            showInfo();
                            Toast.makeText(getApplicationContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            flag = 0;
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                }
            });
        }
    }
}