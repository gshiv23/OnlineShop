package com.example.onlineshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.onlineshop.Helper.TinyDB;
import com.example.onlineshop.R;
import com.example.onlineshop.databinding.ActivityAddressBinding;
import com.example.onlineshop.Domain.DeliveryAddress;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddressActivity extends AppCompatActivity {

    private TextInputEditText fullAddress;
    private Button saveBtn;

    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        fullAddress = findViewById(R.id.fullAddress);
        saveBtn = findViewById(R.id.saveBtn);

        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(auth.getUid());

        loadAddressFromProfile();

        saveBtn.setOnClickListener(v -> saveAddress());
    }

    private void loadAddressFromProfile() {
        userRef.child("address").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    fullAddress.setText(snapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddressActivity.this, "Failed to load address", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAddress() {
        String address = fullAddress.getText().toString().trim();

        if (address.isEmpty()) {
            Toast.makeText(this, "Please enter address", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.child("address").setValue(address).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AddressActivity.this, "Address saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddressActivity.this, "Failed to save address", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

