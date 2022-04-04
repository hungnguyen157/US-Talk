package com.example.ustalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {
    TextView txtTitle;
    EditText editCurrentPassword, editNewPassword, editConfirmNewPassword;
    Button btnSave, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        txtTitle = (TextView) findViewById(R.id.txtTitle);
        editCurrentPassword = (EditText) findViewById(R.id.editCurrentPassword);
        editNewPassword = (EditText) findViewById(R.id.editNewPassword);
        editConfirmNewPassword = (EditText) findViewById(R.id.editConfirmNewPassword);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        //set gradient color for title
        TextPaint txt_paint = txtTitle.getPaint();
        float txt_width = txt_paint.measureText(txtTitle.getText().toString());
        Shader txt_shader = new LinearGradient(0, 0, txt_width, txtTitle.getTextSize(),
                new int[]{
                        Color.parseColor("#F89B29"),
                        Color.parseColor("#F89B29"),
                        Color.parseColor("#E12B4F"),
                        Color.parseColor("#FF0F7B"),
                }, null, Shader.TileMode.CLAMP);
        txtTitle.getPaint().setShader(txt_shader);

        //set OnClickListener event for buttons
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentPass = editCurrentPassword.getText().toString();
                String newPass = editNewPassword.getText().toString();
                String confirmNewPass = editConfirmNewPassword.getText().toString();

                if (currentPass.isEmpty()){
                    makeToast("Xin hãy điền mật khẩu hiện tại");
                }

                else if (newPass.isEmpty()){
                    makeToast("Xin hãy điền mật khẩu mới");
                }
                else if (confirmNewPass.isEmpty()){
                    makeToast("Xin hãy xác nhận lại mật khẩu mới");
                }
                else{
                    //get current password from database
                    String currentPassFromDB = "";

                    if (!currentPass.equals(currentPassFromDB)){
                        makeToast("Vui lòng nhập chính xác mật khẩu hiện tại của bạn");
                    }
                    else{
                        //set new password to database
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        user.updatePassword(newPass)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            makeToast("Đổi mật khẩu thành công");
                                            onBackPressed();
                                        }
                                        else{
                                            makeToast("Đã có lỗi xảy ra khi chúng tôi cố " +
                                                    "đổi mật khẩu cho bạn\n" +
                                                    "Xin hãy kiểm tra lại đường truyền mạng hoặc thử lại sau");
                                        }
                                    }
                                });
                    }
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void makeToast(String message) {
        Toast.makeText(ChangePasswordActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}