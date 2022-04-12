package com.example.ustalk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ustalk.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends Activity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    FirebaseFirestore db;

    TextView txt_forgot_password, signup, txt_label;
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
        txt_forgot_password.setOnClickListener(this);

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
        txt_forgot_password = findViewById(R.id.txt_forgot_password);
        signup = findViewById(R.id.signup);
        txt_label = findViewById(R.id.txt_label);
        logo = findViewById(R.id.logo);
    }

    private void signInFromSession() {
        String uid = prefManager.getString("UID");
        if (uid != null) transition();
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
            case R.id.txt_forgot_password:
                startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class));
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

    private void transition() {
        startActivity(new Intent(getApplicationContext(), ChatHistoryActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    private void makeToast(String message) {
        Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
