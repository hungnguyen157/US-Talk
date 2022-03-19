package com.example.ustalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ustalk.databinding.SignInBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignInActivity extends Activity {
    private FirebaseAuth mAuth;
    private SignInBinding signinBinding;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signinBinding = SignInBinding.inflate(getLayoutInflater());
        setContentView(signinBinding.getRoot());
        setListen();
    }

    private void setListen(){
        signinBinding.signup.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
        });
        signinBinding.BtnSignin.setOnClickListener(view -> addDatatoFirebase());
    }

    private void addDatatoFirebase(){
        Toast.makeText(getApplicationContext(), "Signin Running....", Toast.LENGTH_LONG).show();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> data = new HashMap<>();
        data.put("email", "abc@gmail.com");
        db.collection("users")
                .add(data)
                .addOnSuccessListener(documentReference ->{
                    Toast.makeText(getApplicationContext(), "Inserted", Toast.LENGTH_LONG).show();
                        })
                .addOnFailureListener(exception -> {
                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        });
    }
}
