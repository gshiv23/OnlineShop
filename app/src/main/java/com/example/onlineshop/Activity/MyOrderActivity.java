package com.example.onlineshop.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineshop.Adapter.MyOrderAdapter;
import com.example.onlineshop.Model.OrderModel;
import com.example.onlineshop.databinding.ActivityMyOrderBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class MyOrderActivity extends AppCompatActivity {

    private ActivityMyOrderBinding binding;
    private ArrayList<OrderModel> orderList;
    private MyOrderAdapter adapter;
    private DatabaseReference ordersRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");

        orderList = new ArrayList<>();
        adapter = new MyOrderAdapter(this, orderList);
        binding.orderRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.orderRecycler.setAdapter(adapter);

        binding.backBtn.setOnClickListener(v -> finish());

        loadUserOrders();
    }

    private void loadUserOrders() {
        binding.progressBar.setVisibility(View.VISIBLE);

        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    OrderModel order = snap.getValue(OrderModel.class);
                    if (order != null && currentUserId.equals(order.getUserId())) {
                        orderList.add(order);
                    }
                }

                if (orderList.isEmpty()) {
                    binding.emptyTxt.setVisibility(View.VISIBLE);
                } else {
                    binding.emptyTxt.setVisibility(View.GONE);
                }

                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyOrderActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }
}
