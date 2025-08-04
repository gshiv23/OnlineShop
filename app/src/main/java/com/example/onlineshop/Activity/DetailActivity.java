package com.example.onlineshop.Activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.onlineshop.Domain.PopularDomain;
import com.example.onlineshop.Helper.ManagmentCart;
import com.example.onlineshop.R;
import com.example.onlineshop.databinding.ActivityDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailActivity extends AppCompatActivity {
    private ActivityDetailBinding binding;
    private PopularDomain object;
    private int numberOrder = 1;
    private ManagmentCart managmentCart;

    // ✅ Firebase
    private DatabaseReference wishlistRef;
    private String userId;
    private boolean isWishlisted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        statusBarColor();
        getBundles();
        managmentCart = new ManagmentCart(this);

        // ✅ Firebase Setup
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            wishlistRef = FirebaseDatabase.getInstance().getReference("Wishlists").child(userId);
            checkWishlistStatus();
        }

        // ✅ Add to Cart
        binding.addToCartBtn.setOnClickListener(view -> {
            object.setNumberInCart(numberOrder);
            managmentCart.insertFood(object);
            Toast.makeText(this, "Added to Cart", Toast.LENGTH_SHORT).show();
        });

        // ✅ Back Button
        binding.backBtn.setOnClickListener(view -> finish());

        // ✅ Wishlist Button
        binding.wishlistBtn.setOnClickListener(view -> {
            if (userId == null) {
                Toast.makeText(this, "Login to use wishlist", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isWishlisted) {
                removeFromWishlist();
            } else {
                addToWishlist();
            }
        });
    }

    private void statusBarColor() {
        Window window = DetailActivity.this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(DetailActivity.this, R.color.white));
    }

    private void getBundles() {
        object = (PopularDomain) getIntent().getSerializableExtra("object");

        if (object == null) {
            Toast.makeText(this, "Error loading product details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProductImage(object.getPic());

        binding.titleTxt.setText(object.getTitle());
        binding.priceTxt.setText("Rs." + object.getPrice());
        binding.descriptionTxt.setText(object.getDescription());
        binding.reviewTxt.setText(String.valueOf(object.getReview()));
        binding.ratingTxt.setText(String.valueOf(object.getScore()));
    }

    /**
     * ✅ Universal Image Loader for TinyDB (Drawable, URL, Base64)
     */
    private void loadProductImage(String pic) {
        if (pic == null || pic.isEmpty()) {
            binding.itemPic.setImageResource(R.drawable.ic_placeholder);
            return;
        }

        if (pic.startsWith("http")) {
            // ✅ URL image
            Glide.with(this)
                    .load(pic)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(binding.itemPic);

        } else if (pic.length() > 100) {
            // ✅ Base64 encoded image
            try {
                byte[] decodedBytes = Base64.decode(pic, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                binding.itemPic.setImageBitmap(bitmap);
            } catch (Exception e) {
                binding.itemPic.setImageResource(R.drawable.ic_placeholder);
            }

        } else {
            // ✅ Drawable resource (saved name like "burger1")
            int drawableRes = getResources().getIdentifier(pic, "drawable", getPackageName());
            Glide.with(this)
                    .load(drawableRes != 0 ? drawableRes : R.drawable.ic_placeholder)
                    .into(binding.itemPic);
        }
    }

    private void checkWishlistStatus() {
        if (object.getId() == null) return;

        wishlistRef.child(object.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    isWishlisted = true;
                    binding.wishlistBtn.setImageResource(R.drawable.bookmark_filled);
                } else {
                    isWishlisted = false;
                    binding.wishlistBtn.setImageResource(R.drawable.bookmark_border);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DetailActivity.this, "Failed to load wishlist", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToWishlist() {
        if (object.getId() == null) {
            Toast.makeText(this, "Error: Product ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        wishlistRef.child(object.getId()).setValue(object).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                isWishlisted = true;
                binding.wishlistBtn.setImageResource(R.drawable.bookmark_filled);
                Toast.makeText(this, "Added to Wishlist", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to add to wishlist", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeFromWishlist() {
        if (object.getId() == null) {
            Toast.makeText(this, "Error: Product ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        wishlistRef.child(object.getId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                isWishlisted = false;
                binding.wishlistBtn.setImageResource(R.drawable.bookmark_border);
                Toast.makeText(this, "Removed from Wishlist", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to remove from wishlist", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
