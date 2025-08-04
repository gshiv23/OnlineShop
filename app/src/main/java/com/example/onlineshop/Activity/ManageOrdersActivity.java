package com.example.onlineshop.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineshop.Adapter.ManageOrdersAdapter;
import com.example.onlineshop.Domain.OrderDomain;
import com.example.onlineshop.databinding.ActivityManageOrdersBinding;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageOrdersActivity extends AppCompatActivity {

    private ActivityManageOrdersBinding binding;
    private ManageOrdersAdapter adapter;
    private ArrayList<OrderDomain> orderList = new ArrayList<>();
    private ArrayList<OrderDomain> filteredList = new ArrayList<>();
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseRef = FirebaseDatabase.getInstance().getReference();

        setupRecyclerView();
        setupChipFilter();

        boolean isAdminPassed = getIntent().getBooleanExtra("isAdmin", false);
        if (isAdminPassed) {
            loadAllOrders();
        } else {
            checkUserTypeAndLoadOrders();
        }
    }

    private void setupRecyclerView() {
        binding.manageOrdersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ManageOrdersAdapter(this, filteredList);
        binding.manageOrdersRecyclerView.setAdapter(adapter);
    }

    private void checkUserTypeAndLoadOrders() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseRef.child("Users").child(currentUserId).child("usertype")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String usertype = snapshot.getValue(String.class);
                        if ("admin".equalsIgnoreCase(usertype)) {
                            loadAllOrders();
                        } else {
                            Toast.makeText(ManageOrdersActivity.this,
                                    "Access denied: You are not an admin.",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ManageOrdersActivity.this,
                                "Failed to verify usertype: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void loadAllOrders() {
        binding.progressBar.setVisibility(View.VISIBLE);
        databaseRef.child("Orders")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderList.clear();
                        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                            OrderDomain order = orderSnapshot.getValue(OrderDomain.class);
                            if (order != null) {
                                order.setOrderId(orderSnapshot.getKey());
                                orderList.add(order);
                            }
                        }
                        filterOrders("All");
                        binding.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(ManageOrdersActivity.this,
                                "Error loading orders: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void setupChipFilter() {
        binding.chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            String status = "All";
            Chip chip = findViewById(checkedId);
            if (chip != null) status = chip.getText().toString();
            filterOrders(status);
        });
    }

    private void filterOrders(String status) {
        filteredList.clear();
        for (OrderDomain order : orderList) {
            if ("All".equalsIgnoreCase(status) || order.getStatus().equalsIgnoreCase(status)) {
                filteredList.add(order);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
