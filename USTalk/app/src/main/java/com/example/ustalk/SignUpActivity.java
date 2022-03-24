package com.example.ustalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    EditText editEmail, editPassword, editRepeatPassword, editName;
    Button btnSignUp;
    RadioGroup radioGroup;
    FirebaseAuth mAuth;
    TextView signIn, txt_label;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editPassword = (EditText) findViewById(R.id.editPassword);
        editRepeatPassword = (EditText) findViewById(R.id.editRepeatPassword);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        signIn = (TextView) findViewById(R.id.signIn);
        mAuth = FirebaseAuth.getInstance();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        editName = (EditText) findViewById(R.id.editName);
        radioGroup = (RadioGroup) findViewById(R.id.rbtngr);

        //Make the label become gradient
        txt_label = (TextView) findViewById(R.id.txt_label);
        TextPaint txt_paint = txt_label.getPaint();
        float txt_width = txt_paint.measureText(txt_label.getText().toString());
        Shader txt_shader = new LinearGradient(0, 0, txt_width, txt_label.getTextSize(),
                new int[]{
                        Color.parseColor("#40C9FF"),
                        Color.parseColor("#E81CFF"),
                }, null, Shader.TileMode.CLAMP);
        txt_label.getPaint().setShader(txt_shader);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editEmail.getText().toString().trim();
                String password = editPassword.getText().toString().trim();
                String password2 = editRepeatPassword.getText().toString().trim();
                String name = editName.getText().toString().trim();

                if (name.isEmpty()) makeToast("Please input name");
                else if (email.isEmpty()) makeToast("Please input email");
                else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) makeToast("Enter valid email");
                else if (password.isEmpty()) makeToast("Please input password");
                else if (password.length() < 6) makeToast("Password is too short");
                else if (password2.isEmpty()) makeToast("Please input confirm password");
                else if (!password.equals(password2)) makeToast("Password and confirm password are not match");
                else if (radioGroup.getCheckedRadioButtonId() == -1) makeToast("Please select one of the gender");
                else{
                    // pass validation
                    progressBar.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(email,password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        User user = new User("New User","-1", email);
                                        FirebaseDatabase.getInstance("https://us-talk-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    progressBar.setVisibility(View.GONE);
                                                    makeToast("Sign up successfully");
                                                    startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                                                } else {
                                                    progressBar.setVisibility(View.GONE);
                                                    makeToast("Something wrong");
                                                }
                                            }
                                        });
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        makeToast("Something wrong");
                                    }
                                }
                            });
                }
            }

            private void makeToast(String message) {
                Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
