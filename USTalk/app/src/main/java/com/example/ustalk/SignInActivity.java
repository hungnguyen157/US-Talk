package com.example.ustalk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ustalk.models.User;
import com.example.ustalk.utilities.CurrentUserDetails;
import com.example.ustalk.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignInActivity extends Activity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
//    private SignInBinding signinBinding;

    TextView signup, txt_label;
    ImageView logo;
    EditText editEmail, editPassword;
    Button btnSignIn;
    ProgressBar progressBar;
    PreferenceManager prefManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        signInFromSession();

//        signinBinding = SignInBinding.inflate(getLayoutInflater());
//        setContentView(signinBinding.getRoot());
//        setListen();

        getViewRef();
        btnSignIn.setOnClickListener(this);
        signup.setOnClickListener(this);

        //Make the label become gradient
        TextPaint txt_paint = txt_label.getPaint();
        float txt_width = txt_paint.measureText(txt_label.getText().toString());
        Shader txt_shader = new LinearGradient(0, 0, txt_width, txt_label.getTextSize(),
                new int[]{
                        Color.parseColor("#F89B29"),
                        Color.parseColor("#F89B29"),
                        Color.parseColor("#E12B4F"),
                        Color.parseColor("#FF0F7B"),
                }, null, Shader.TileMode.CLAMP);
        txt_label.getPaint().setShader(txt_shader);

        //Resize logo
        logo.getLayoutParams().width = (int)(txt_width * 0.714285714);
    }

    private void getViewRef() {
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editPassword = (EditText) findViewById(R.id.editPassword);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        signup = (TextView) findViewById(R.id.signup);
        txt_label = (TextView) findViewById(R.id.txt_label);
        logo = (ImageView) findViewById(R.id.logo);
    }

    private void signInFromSession() {
        prefManager = new PreferenceManager(this);
        String uid = prefManager.getString("UID");
        if (uid != null) {
            db.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        User user = task.getResult().toObject(User.class);
                        CurrentUserDetails.getInstance().setUser(user);
                        transition();
                    }
                    else {
                        Log.w("signin", "Error getting document.", task.getException());
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btnSignIn:
                String email = editEmail.getText().toString().trim();
                String password = editPassword.getText().toString().trim();

                if (email.isEmpty()) makeToast("Please input email");
                else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) makeToast("Enter valid email");
                else if (password.isEmpty()) makeToast("Please input password");

                else{
                    progressBar.setVisibility(View.VISIBLE);
                    btnSignIn.setEnabled(false);
                    String btnText = btnSignIn.getText().toString();
                    btnSignIn.setText("");
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        transition();
                                        progressBar.setVisibility(View.GONE);
                                        prefManager.putString("UID", mAuth.getCurrentUser().getUid());
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        makeToast("Sign In failed! Check your credentials");
                                    }
                                    btnSignIn.setEnabled(true);
                                    btnSignIn.setText(btnText);
                                }
                            });
                }
                break;
            case R.id.signup:
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
                break;
        }
    }

    private void transition() {
        startActivity(new Intent(getApplicationContext(), ChatHistoryActivity.class));
    }

    private void makeToast(String message) {
        Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();
    }

//    private void setListen(){
//        signinBinding.signup.setOnClickListener(view -> {
//            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
//        });
//        signinBinding.BtnSignin.setOnClickListener(view -> addDatatoFirebase());
//    }
//
//    private void addDatatoFirebase(){
//        Toast.makeText(getApplicationContext(), "Signin Running....", Toast.LENGTH_LONG).show();
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        HashMap<String, Object> data = new HashMap<>();
//        data.put("email", "abc@gmail.com");
//        db.collection("users")
//                .add(data)
//                .addOnSuccessListener(documentReference ->{
//                    Toast.makeText(getApplicationContext(), "Inserted", Toast.LENGTH_LONG).show();
//                        })
//                .addOnFailureListener(exception -> {
//                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
//                        });
//    }
}
