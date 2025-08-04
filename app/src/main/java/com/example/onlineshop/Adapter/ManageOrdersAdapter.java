package com.example.onlineshop.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineshop.Domain.OrderDomain;
import com.example.onlineshop.databinding.OrderItemAdminBinding;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ManageOrdersAdapter extends RecyclerView.Adapter<ManageOrdersAdapter.OrderViewHolder> {

    private final Context context;
    private final ArrayList<OrderDomain> orderList;
    private final String[] statusOptions = {"Pending", "Processing", "Shipped", "Out for Delivery", "Delivered", "Cancelled"};

    public ManageOrdersAdapter(Context context, ArrayList<OrderDomain> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrderViewHolder(OrderItemAdminBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderDomain order = orderList.get(position);
        OrderItemAdminBinding b = holder.binding;

        // Display basic order info
        b.orderIdTxt.setText("Order ID: " + order.getOrderId());
        b.totalAmountTxt.setText("₹" + order.getTotalAmount());
        b.addressTxt.setText("Address: " + order.getAddress());
        b.phoneTxt.setText("Phone: " + order.getPhone());

        // Format and display date
        try {
            String formattedDate = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())
                    .format(new Date(order.getTimestamp()));
            b.dateTxt.setText("Date: " + formattedDate);
        } catch (Exception e) {
            b.dateTxt.setText("Date: N/A");
        }

        // Show products in readable format
        StringBuilder itemsBuilder = new StringBuilder();
        ArrayList<HashMap<String, Object>> items = order.getItems();

        if (items != null && !items.isEmpty()) {
            for (HashMap<String, Object> product : items) {
                Object titleObj = product.get("title");
                if (titleObj != null) {
                    itemsBuilder.append("• ").append(titleObj.toString()).append("\n");
                } else {
                    itemsBuilder.append("• (Unnamed Product)\n");
                }
            }
        } else {
            itemsBuilder.append("No products found");
        }
        b.productsTxt.setText(itemsBuilder.toString().trim());

        // Show current status
        b.statusTxt.setText("Status: " + order.getStatus());

        // Setup status dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        b.statusSpinner.setAdapter(adapter);

        // Pre-select the current status
        int selectedIndex = 0;
        for (int i = 0; i < statusOptions.length; i++) {
            if (statusOptions[i].equalsIgnoreCase(order.getStatus())) {
                selectedIndex = i;
                break;
            }
        }
        b.statusSpinner.setSelection(selectedIndex);

        // Handle status update
        b.updateBtn.setOnClickListener(v -> {
            String selectedStatus = b.statusSpinner.getSelectedItem().toString();
            FirebaseDatabase.getInstance().getReference("Orders")
                    .child(order.getOrderId())
                    .child("status")
                    .setValue(selectedStatus)
                    .addOnSuccessListener(unused -> {
                        order.setStatus(selectedStatus);
                        notifyItemChanged(position);
                        Toast.makeText(context, "Status updated to " + selectedStatus, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // Handle order deletion
        b.deleteBtn.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("Orders")
                    .child(order.getOrderId())
                    .removeValue()
                    .addOnSuccessListener(unused -> {
                        orderList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, orderList.size());
                        Toast.makeText(context, "Order deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        OrderItemAdminBinding binding;

        public OrderViewHolder(OrderItemAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
