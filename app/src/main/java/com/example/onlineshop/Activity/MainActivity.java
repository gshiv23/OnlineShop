package com.example.onlineshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineshop.Adapter.CategoryAdapter;
import com.example.onlineshop.Adapter.PopularAdapter;
import com.example.onlineshop.Domain.CategoryDomain;
import com.example.onlineshop.R;
import com.example.onlineshop.databinding.ActivityMainBinding;
import com.example.onlineshop.Domain.PopularDomain;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseUser currentUser;
    private DatabaseReference userRef, categoryRef;

    private PopularAdapter popularAdapter;
    private CategoryAdapter categoryAdapter;
    private ArrayList<PopularDomain> items = new ArrayList<>();
    private ArrayList<CategoryDomain> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        statusBarColor();

        // ✅ Firebase reference
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        categoryRef = FirebaseDatabase.getInstance().getReference("Categories");

        setupCategoryRecyclerView();
        loadCategoriesFromFirebase();

        // ✅ Initialize UI
        setupRecyclerView();
        loadPopularProducts();
        bottomNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        updateWelcomeUser();
    }

    private void bottomNavigation() {
        binding.cartBtn.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, CartActivity.class)));

        binding.profileBtn.setOnClickListener(view -> {
            if (currentUser != null) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        binding.wishlistBtn.setOnClickListener(view -> {
            if (currentUser != null) {
                startActivity(new Intent(MainActivity.this, WishlistActivity.class));
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }

    private void statusBarColor() {
        Window window = MainActivity.this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.blue_Dark));
    }

    private void setupRecyclerView() {
        popularAdapter = new PopularAdapter(items);
        binding.PopularView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.PopularView.setAdapter(popularAdapter);
    }

    // ✅ Setup Category RecyclerView
    private void setupCategoryRecyclerView() {
        categoryAdapter = new CategoryAdapter(categories);
        binding.categoryRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.categoryRecyclerView.setAdapter(categoryAdapter);
    }

    // ✅ Load Categories from Firebase
    private void loadCategoriesFromFirebase() {
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categories.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        CategoryDomain category = data.getValue(CategoryDomain.class);
                        if (category != null) {
                            categories.add(category);
                        }
                    }
                    categoryAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "No categories found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadPopularProducts() {
        // ✅ Load only once, avoids recreating items list
        if (!items.isEmpty()) return;

        // ✅ Updated constructor to include id + double for score & price
        items.add(new PopularDomain(
                "1", // ✅ Unique ID
                "T-Shirt Black",
                "item_1",
                15,
                4.0, // ✅ Double instead of int
                500.0, // ✅ Double instead of int
                "Material: 180 GSM, 100% Combed Cotton (Bio-Washed and Pre-Shrunk)\n" +
                        "Neck: Crew Neck\n" +
                        "Sleeves: Short Sleeves\n" +
                        "Features:\n• Bio-Washed\n• Pre-shrunk\n• Breathable\n• Durable\nOrigin: India"
        ));

        items.add(new PopularDomain(
                "2",
                "Smart Watch",
                "item_2",
                10,
                4.5,
                450.0,
                "Ultra-narrow bezels, 77.4% screen ratio, 9.9mm slim, 26g, aluminum alloy design."
        ));

        items.add(new PopularDomain(
                "3",
                "Phone",
                "item_3",
                3,
                4.9,
                800.0,
                "Portable smartphone with voice, multimedia, apps, and internet access."
        ));

        popularAdapter.notifyDataSetChanged();
    }

    private void updateWelcomeUser() {
        if (currentUser != null) {
            userRef.child(currentUser.getUid()).child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            String name = snapshot.getValue(String.class);
                            if (name != null && !name.isEmpty()) {
                                binding.textView2.setText("Hi, " + name + "!");
                            } else {
                                binding.textView2.setText("Welcome!");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            binding.textView2.setText("Welcome!");
                        }
                    });
        } else {
            binding.textView2.setText("Welcome Guest!");
        }
    }
}
