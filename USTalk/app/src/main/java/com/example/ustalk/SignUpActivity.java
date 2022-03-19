package com.example.ustalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.auth.User;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    EditText editEmail, editPassword, editRepeatPassword;
    Button btnSignUp;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editPassword = (EditText) findViewById(R.id.editPassword);
        editRepeatPassword = (EditText) findViewById(R.id.editRepeatPassword);
        btnSignUp = (Button) findViewById(R.id.BtnSignin);
        mAuth = FirebaseAuth.getInstance();
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editEmail.getText().toString().trim();
                String password = editPassword.getText().toString().trim();
                if (email.isEmpty())
                    Toast.makeText(SignUpActivity.this, "Please input email", Toast.LENGTH_SHORT).show();
                else if(!Patterns.EMAIL_ADDRESS.matcher(editEmail.getText().toString()).matches())
                    Toast.makeText(SignUpActivity.this, "Enter valid email", Toast.LENGTH_SHORT).show();
                else if (editPassword.getText().toString().trim().isEmpty())
                    Toast.makeText(SignUpActivity.this, "Please input password", Toast.LENGTH_SHORT).show();
                else if (editRepeatPassword.getText().toString().trim().isEmpty())
                    Toast.makeText(SignUpActivity.this, "Please input confirm password", Toast.LENGTH_SHORT).show();
                else if (!editPassword.getText().toString().equals(editRepeatPassword.getText().toString()))
                    Toast.makeText(SignUpActivity.this, "Password and confirm password are not match", Toast.LENGTH_SHORT).show();
                else{
                    // pass validation
//                    mAuth.createUserWithEmailAndPassword(email,password)
//                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                                @Override
//                                public void onComplete(@NonNull Task<AuthResult> task) {
//                                    if (task.isSuccessful()) {
//                                        User user = new User(email,"Thông",18);
//                                        FirebaseDatabase.getInstance().getReference("Users")
//                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                                                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                if (task.isSuccessful()){
//                                                    Toast.makeText(SignUpActivity.this, "Sign up successfully", Toast.LENGTH_SHORT).show();
//                                                }
//                                            }
//                                        });
//                                    } else
//                                        Toast.makeText(SignUpActivity.this, "Something wrong", Toast.LENGTH_SHORT).show();
//                                }
//                            });
                }
            }
        });
    }
}
