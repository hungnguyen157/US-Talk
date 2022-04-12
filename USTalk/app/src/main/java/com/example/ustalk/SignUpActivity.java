package com.example.ustalk;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ustalk.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {
    EditText editEmail, editPassword, editRepeatPassword, editName;
    Button btnSignUp;
    RadioGroup radioGroup;
    FirebaseAuth mAuth;
    TextView signIn, txt_label;
    ProgressBar progressBar;
    RadioButton rbtnMale, rbtnFemale;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

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
        rbtnFemale = (RadioButton) findViewById(R.id.rbtn_female);
        rbtnMale = (RadioButton) findViewById(R.id.rbtn_male);

        DatabaseReference data = FirebaseDatabase.getInstance("https://us-talk-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Users");

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
                onBackPressed();
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
                    btnSignUp.setEnabled(false);
                    String btnText = btnSignUp.getText().toString();
                    btnSignUp.setText("");
                    mAuth.createUserWithEmailAndPassword(email,password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String sex = "Female", imageProfile = "https://firebasestorage.googleapis.com/v0/b/us-talk.appspot.com/o/Avatar%2Fwoman.png?alt=media&token=c6651709-ed7f-4664-a06e-b20c6b8639fe";
                                    if (rbtnMale.isChecked()) {
                                        sex = "Male";
                                        imageProfile = "https://firebasestorage.googleapis.com/v0/b/us-talk.appspot.com/o/Avatar%2Fman.png?alt=media&token=41d34fff-f4ea-4b0f-b22c-2037af74c9f6";
                                    }
                                    User user = new User(name, email, sex, imageProfile);
                                    String uid = mAuth.getCurrentUser().getUid();

                                    db.collection("users").document(uid).set(user)
                                            .addOnCompleteListener(task1 -> {
                                                progressBar.setVisibility(View.GONE);
                                                btnSignUp.setEnabled(true);
                                                btnSignUp.setText(btnText);
                                                if (task1.isSuccessful()) {
                                                    makeToast("Đăng kí thành công");
                                                    onBackPressed();
                                                }
                                                else makeToast("Đăng kí không thành công");
                                            });
                                } else {
                                    makeToast(task.getException().getMessage());
                                    progressBar.setVisibility(View.GONE);
                                    btnSignUp.setEnabled(true);
                                    btnSignUp.setText(btnText);
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
