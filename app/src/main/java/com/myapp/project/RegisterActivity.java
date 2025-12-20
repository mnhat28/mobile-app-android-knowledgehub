package com.myapp.project;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tạo tài khoản...");
        progressDialog.setCancelable(false);

        btnRegister.setOnClickListener(v -> registerUser());

        animateViews();
    }

    private void animateViews() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        etEmail.startAnimation(fadeIn);
        etPassword.startAnimation(fadeIn);
        etConfirmPassword.startAnimation(fadeIn);
        btnRegister.startAnimation(fadeIn);
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Nhập email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Nhập password");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Password không khớp");
            return;
        }

        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        FirebaseAuth.getInstance().signOut();
                        // Thông báo và quay lại LoginActivity
                        Toast.makeText(RegisterActivity.this,
                                "Đăng ký thành công! Vui lòng đăng nhập.",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Lỗi: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void finish() {
        super.finish();
        // Hiệu ứng chuyển màn hình
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
