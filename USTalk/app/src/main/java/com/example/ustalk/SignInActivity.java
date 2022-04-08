package com.example.ustalk;

import android.app.Activity;
import android.content.Intent;
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

import com.example.ustalk.models.User;
import com.example.ustalk.utilities.CurrentUserDetails;
import com.example.ustalk.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class SignInActivity extends Activity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    FirebaseFirestore db;

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
        prefManager = new PreferenceManager(this);

        signInFromSession();
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
        btnSignIn = findViewById(R.id.btnSignIn);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        signup = findViewById(R.id.signup);
        txt_label = findViewById(R.id.txt_label);
        logo = findViewById(R.id.logo);
    }

    private void signInFromSession() {
        String uid = prefManager.getString("UID");
        if (uid != null) {
            transition();
        updateUserToken(user, uid);}
    }

    private void updateUserToken(User user, String uid) {
        if (user.token == null) {
            FirebaseMessaging.getInstance().getToken()
                    .addOnSuccessListener(token -> updateToken(uid, token))
                    .addOnFailureListener(e -> Log.e("updateToken", e.getMessage()));
        }
        else {
            String token = prefManager.getString("token");
            if (token != null) {
                updateToken(uid, token);
                prefManager.remove("token");
            }
        }
    }

    private void updateToken(String uid, String token) {
        db.collection("users").document(uid).update("token", token)
                .addOnFailureListener(e -> Log.e("token", e.getMessage()));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btnSignIn:
                signIn();
                break;
            case R.id.signup:
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
                break;
        }
    }

    private void signIn() {
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
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            String uid = mAuth.getCurrentUser().getUid();
                            prefManager.putString("UID", uid);
                            transition();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            makeToast("Sign In failed! Check your credentials");
                        }
                        btnSignIn.setEnabled(true);
                        btnSignIn.setText(btnText);
                    });
        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        Log.d("online", "Sign in onPause");
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.d("online", "Sign in onResume");
//    }

    private void transition() {
        startActivity(new Intent(getApplicationContext(), ChatHistoryActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    private void makeToast(String message) {
        Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
