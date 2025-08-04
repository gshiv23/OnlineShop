package com.example.onlineshop.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlineshop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private ImageView splashLogo;
    private TextView splashAppName, splashTagline;
    private RelativeLayout logoContainer;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashLogo = findViewById(R.id.splashLogo);
        splashAppName = findViewById(R.id.splashAppName);
        splashTagline = findViewById(R.id.splashTagline);
        logoContainer = findViewById(R.id.logoContainer);

        mAuth = FirebaseAuth.getInstance();

        // ✅ Load animations
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        logoContainer.startAnimation(scaleUp);
        splashAppName.startAnimation(fadeIn);
        splashTagline.startAnimation(fadeIn);

        // ✅ Delay for splash screen (3 seconds)
        new Handler().postDelayed(this::checkUserStatus, 3000);
    }

    private void checkUserStatus() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isAdmin = prefs.getBoolean("is_admin", false);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            if (isAdmin) {
                // ✅ Auto-login Admin if still authenticated
                startActivity(new Intent(SplashActivity.this, AdminDashboardActivity.class));
            } else {
                // ✅ Auto-login Normal User
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
        } else {
            // ✅ New/Logged-out user → Onboarding or Login
            startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
        }

        finish();
    }
}
