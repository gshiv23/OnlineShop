package com.example.onlineshop.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.onlineshop.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private ShapeableImageView profileImage;
    private EditText editName, editPhone, editGender, editDOB, editAddress, editBio;
    private TextView profileEmail;
    private Button updateProfileBtn, logoutBtn, helpBtn, myOrdersBtn;

    private FirebaseUser currentUser;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // View bindings
        profileImage = findViewById(R.id.profileImage);
        editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        editGender = findViewById(R.id.editGender);
        editDOB = findViewById(R.id.editDOB);
        editAddress = findViewById(R.id.editAddress);
        editBio = findViewById(R.id.editBio);
        profileEmail = findViewById(R.id.profileEmail);
        updateProfileBtn = findViewById(R.id.updateNameBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        helpBtn = findViewById(R.id.helpBtn);
        myOrdersBtn = findViewById(R.id.myOrdersBtn);


        // Firebase setup
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        loadProfileData();

        // Change profile picture
        profileImage.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .cropSquare()
                    .compress(1024)
                    .maxResultSize(512, 512)
                    .start();
        });

        // Update all profile fields
        updateProfileBtn.setOnClickListener(v -> updateProfileData());

        // Logout
        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });

        // Help
        helpBtn.setOnClickListener(v ->
                Toast.makeText(this, "Help & Support coming soon", Toast.LENGTH_SHORT).show());

        // My Orders
        myOrdersBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MyOrderActivity.class);
            startActivity(intent);
        });

    }

    // Handle Image Selection and Update
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                profileImage.setImageBitmap(bitmap); // Preview

                // Convert to Base64
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                byte[] imageBytes = baos.toByteArray();
                String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                // Save Base64 string in Realtime Database
                userRef.child("profileImageBase64").setValue(encodedImage)
                        .addOnSuccessListener(unused ->
                                Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to update image", Toast.LENGTH_SHORT).show());

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Load user profile data
    private void loadProfileData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Load profile image
                String encodedImage = snapshot.child("profileImageBase64").getValue(String.class);
                if (encodedImage != null && !encodedImage.isEmpty()) {
                    byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    profileImage.setImageBitmap(bitmap);
                } else {
                    profileImage.setImageResource(R.drawable.ic_user_avatar);
                }

                // Load other fields
                if (snapshot.child("name").getValue() != null)
                    editName.setText(snapshot.child("name").getValue(String.class));

                if (snapshot.child("phoneNumber").getValue() != null)
                    editPhone.setText(snapshot.child("phoneNumber").getValue(String.class));

                if (snapshot.child("gender").getValue() != null)
                    editGender.setText(snapshot.child("gender").getValue(String.class));

                if (snapshot.child("dob").getValue() != null)
                    editDOB.setText(snapshot.child("dob").getValue(String.class));

                if (snapshot.child("address").getValue() != null)
                    editAddress.setText(snapshot.child("address").getValue(String.class));

                if (snapshot.child("bio").getValue() != null)
                    editBio.setText(snapshot.child("bio").getValue(String.class));

                // Load email (fallback to FirebaseAuth email)
                String email = snapshot.child("email").getValue(String.class);
                if (email != null) {
                    profileEmail.setText(email);
                } else {
                    profileEmail.setText(currentUser.getEmail());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Update profile details
    private void updateProfileData() {
        String name = editName.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String gender = editGender.getText().toString().trim();
        String dob = editDOB.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String bio = editBio.getText().toString().trim();

        if (!name.isEmpty()) userRef.child("name").setValue(name);
        if (!phone.isEmpty()) userRef.child("phoneNumber").setValue(phone);
        if (!gender.isEmpty()) userRef.child("gender").setValue(gender);
        if (!dob.isEmpty()) userRef.child("dob").setValue(dob);
        if (!address.isEmpty()) userRef.child("address").setValue(address);
        if (!bio.isEmpty()) userRef.child("bio").setValue(bio);

        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }
}
