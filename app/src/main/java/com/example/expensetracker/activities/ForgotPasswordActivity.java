package com.example.expensetracker.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetracker.R;
import com.example.expensetracker.firebase.FirebaseAuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Handles password reset email dispatch via Firebase.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private ImageView ivBackForgot;
    private TextInputEditText etForgotEmail;
    private MaterialButton btnSendResetLink;
    private ProgressBar pbForgotLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        ivBackForgot = findViewById(R.id.ivBackForgot);
        etForgotEmail = findViewById(R.id.etForgotEmail);
        btnSendResetLink = findViewById(R.id.btnSendResetLink);
        pbForgotLoading = findViewById(R.id.pbForgotLoading);

        ivBackForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSendResetLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etForgotEmail.getText() != null ? etForgotEmail.getText().toString().trim() : "";
                if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etForgotEmail.setError("Enter a valid email address");
                    etForgotEmail.requestFocus();
                    return;
                }

                pbForgotLoading.setVisibility(View.VISIBLE);
                btnSendResetLink.setEnabled(false);

                FirebaseAuthManager.getInstance(ForgotPasswordActivity.this).resetPassword(email, new FirebaseAuthManager.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        pbForgotLoading.setVisibility(View.GONE);
                        btnSendResetLink.setEnabled(true);
                        Toast.makeText(ForgotPasswordActivity.this, "Password reset instructions sent to " + email, Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        pbForgotLoading.setVisibility(View.GONE);
                        btnSendResetLink.setEnabled(true);
                        Toast.makeText(ForgotPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
