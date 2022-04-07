package com.example.ustalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends OnlineActivity {
    TextView txtTitle;
    EditText editEmail;
    Button btnNext;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        txtTitle = (TextView) findViewById(R.id.txtTitle);
        editEmail = (EditText) findViewById(R.id.editEmail);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setEnabled(false);

        //set gradient color for title
        TextPaint txt_paint = txtTitle.getPaint();
        float txt_width = txt_paint.measureText(txtTitle.getText().toString());
        Shader txt_shader = new LinearGradient(0, 0, txt_width, txtTitle.getTextSize(),
                new int[]{
                        Color.parseColor("#40C9FF"),
                        Color.parseColor("#E81CFF"),
                }, null, Shader.TileMode.CLAMP);
        txtTitle.getPaint().setShader(txt_shader);

        //set afterChange event for editEmail
        editEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                email = editable.toString().trim();
                if (!email.isEmpty()){
                    if (Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                        btnNext.setEnabled(true);
                        return;
                    }
                }
                btnNext.setEnabled(false);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //send reset password mail
                FirebaseAuth auth = FirebaseAuth.getInstance();

                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    makeToast("Xin hãy check mail để truy cập đường link " +
                                            "giúp bạn đặt lại mật khẩu");
                                    onBackPressed();
                                }
                                else{
                                    makeToast("Đã có lỗi xảy ra khi chúng tôi cố gửi cho bạn " +
                                            "đường link đặt lại mật khẩu.\n" +
                                            "Xin hãy kiểm tra lại đường truyền mạng hoặc thử lại sau");
                                }
                            }
                        });
            }
        });
    }

    private void makeToast(String message) {
        Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}