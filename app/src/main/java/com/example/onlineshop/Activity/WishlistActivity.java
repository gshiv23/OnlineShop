package com.example.onlineshop.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineshop.Adapter.WishlistAdapter;
import com.example.onlineshop.databinding.ActivityWishlistBinding;
import com.example.onlineshop.Domain.PopularDomain;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class WishlistActivity extends AppCompatActivity {

    private ActivityWishlistBinding binding;
    private WishlistAdapter adapter;
    private ArrayList<PopularDomain> wishlistList = new ArrayList<>();
    private DatabaseReference wishlistRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWishlistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ✅ Firebase Setup
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            wishlistRef = FirebaseDatabase.getInstance().getReference("Wishlists").child(userId);
        } else {
            Toast.makeText(this, "Please log in to view wishlist", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupRecyclerView();
        loadWishlist();

        // ✅ Back Button (optional, if you have one in XML)
        binding.toolbar.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        binding.wishlistRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WishlistAdapter(this, wishlistList, wishlistRef);
        binding.wishlistRecycler.setAdapter(adapter);
    }

    private void loadWishlist() {
        binding.progressBar.setVisibility(View.VISIBLE);

        wishlistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wishlistList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PopularDomain product = dataSnapshot.getValue(PopularDomain.class);
                    if (product != null) {
                        wishlistList.add(product);
                    }
                }
                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);

                // ✅ Show empty view if no wishlist items
                if (wishlistList.isEmpty()) {
                    binding.emptyView.setVisibility(View.VISIBLE);
                } else {
                    binding.emptyView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(WishlistActivity.this, "Failed to load wishlist", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
