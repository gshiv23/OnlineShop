package com.example.onlineshop.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.onlineshop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private Spinner loginUserTypeSpinner;
    private Button loginBtn;
    private TextView goToSignup, forgotPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Auto-login if session exists
        if (mAuth.getCurrentUser() != null) {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            boolean isAdmin = prefs.getBoolean("is_admin", false);

            Intent intent = isAdmin
                    ? new Intent(LoginActivity.this, AdminDashboardActivity.class)
                    : new Intent(LoginActivity.this, MainActivity.class);

            startActivity(intent);
            finish();
            return;
        }

        // Initialize views
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        loginUserTypeSpinner = findViewById(R.id.loginUserTypeSpinner);
        loginBtn = findViewById(R.id.loginBtn);
        goToSignup = findViewById(R.id.goToSignup);
        forgotPassword = findViewById(R.id.forgotPassword);

        // Setup spinner values
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"User", "Admin"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loginUserTypeSpinner.setAdapter(adapter);

        loginBtn.setOnClickListener(v -> loginUser());

        goToSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });

        forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    private void loginUser() {
        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();
        String selectedUserType = loginUserTypeSpinner.getSelectedItem().toString().toLowerCase();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmail.setError("Valid email required");
            loginEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            loginPassword.setError("Password must be 6+ characters");
            loginPassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            fetchUserProfileAndRedirect(currentUser.getUid(), selectedUserType);
                        }
                    } else {
                        Toast.makeText(this, "Authentication failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserProfileAndRedirect(String userId, String selectedType) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(LoginActivity.this, "User profile not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String userType = snapshot.child("userType").getValue(String.class);

                if (email != null && userType != null) {
                    if (!userType.equalsIgnoreCase(selectedType)) {
                        Toast.makeText(LoginActivity.this,
                                "Login failed: Account is registered as a " + userType,
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    boolean isAdmin = "admin".equalsIgnoreCase(userType);

                    saveSession(name != null ? name : "User", email, isAdmin);

                    Toast.makeText(LoginActivity.this,
                            "Welcome, " + (name != null ? name : userType),
                            Toast.LENGTH_SHORT).show();

                    Intent intent = isAdmin
                            ? new Intent(LoginActivity.this, AdminDashboardActivity.class)
                            : new Intent(LoginActivity.this, MainActivity.class);

                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Incomplete user data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this,
                        "Failed to load profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveSession(String name, String email, boolean isAdmin) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit()
                .putString("user_name", name)
                .putString("user_email", email)
                .putBoolean("is_admin", isAdmin)
                .apply();
    }
}
