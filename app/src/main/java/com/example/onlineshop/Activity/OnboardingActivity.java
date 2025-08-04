package com.example.onlineshop.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.onlineshop.Adapter.OnboardingAdapter;
import com.example.onlineshop.Adapter.OnboardingItem;
import com.example.onlineshop.R;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 onboardingViewPager;
    private LinearLayout dotsLayout;
    private TextView skipBtn;
    private Button nextBtn;
    private OnboardingAdapter adapter;
    private TextView[] dots;
    private int currentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        onboardingViewPager = findViewById(R.id.onboardingViewPager);
        dotsLayout = findViewById(R.id.dotsLayout);
        skipBtn = findViewById(R.id.skipBtn);
        nextBtn = findViewById(R.id.nextBtn);

        setupOnboardingItems();
        addDots(0);

        onboardingViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                addDots(position);
                nextBtn.setText(position == adapter.getItemCount() - 1 ? "Get Started" : "Next");
            }
        });

        nextBtn.setOnClickListener(v -> {
            if (currentPosition < adapter.getItemCount() - 1) {
                onboardingViewPager.setCurrentItem(currentPosition + 1);
            } else {
                startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
                finish();
            }
        });

        skipBtn.setOnClickListener(v -> {
            startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
            finish();
        });
    }

    private void setupOnboardingItems() {
        List<OnboardingItem> items = new ArrayList<>();

        items.add(new OnboardingItem(R.drawable.choose_product));
        items.add(new OnboardingItem(R.drawable.make_payment));
        items.add(new OnboardingItem(R.drawable.get_order));

        adapter = new OnboardingAdapter(items);
        onboardingViewPager.setAdapter(adapter);
    }

    private void addDots(int position) {
        dots = new TextView[3];
        dotsLayout.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText("•");
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.grey));
            dotsLayout.addView(dots[i]);
        }
        dots[position].setTextColor(getResources().getColor(R.color.blue_primary));
    }
}
