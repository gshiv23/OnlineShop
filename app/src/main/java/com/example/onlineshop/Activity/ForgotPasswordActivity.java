package com.example.onlineshop.Activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.onlineshop.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText resetEmail;
    private Button resetBtn;
    private TextView backToLogin;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase & UI
        mAuth = FirebaseAuth.getInstance();
        resetEmail = findViewById(R.id.resetEmail);
        resetBtn = findViewById(R.id.resetBtn);
        backToLogin = findViewById(R.id.backToLogin);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending reset link...");
        progressDialog.setCancelable(false);

        resetBtn.setOnClickListener(v -> sendResetEmail());
        backToLogin.setOnClickListener(v -> finish());
    }

    private void sendResetEmail() {
        String email = resetEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            resetEmail.setError("Enter a valid email address");
            resetEmail.requestFocus();
            return;
        }

        progressDialog.show();
        resetBtn.setEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    resetBtn.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "✅ Reset link sent to your email", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errorMessage = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Something went wrong. Try again!";
                        Toast.makeText(this, "❌ Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
