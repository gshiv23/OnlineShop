package com.example.onlineshop.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineshop.Model.OrderModel;
import com.example.onlineshop.databinding.OrderItemUserBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<OrderModel> orderList;

    public MyOrderAdapter(Context context, ArrayList<OrderModel> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        OrderItemUserBinding binding = OrderItemUserBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel order = orderList.get(position);
        holder.binding.orderIdTxt.setText("Order ID: #" + order.getOrderId());

        // Format timestamp to readable date
        String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(new Date(order.getTimestamp()));
        holder.binding.dateTxt.setText("Ordered on: " + formattedDate);

        holder.binding.statusTxt.setText("Status: " + order.getStatus());
        holder.binding.totalTxt.setText("Total: ₹" + order.getTotalAmount());

        // Extract and join product names from items list
        ArrayList<HashMap<String, Object>> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            StringBuilder productNames = new StringBuilder();
            for (HashMap<String, Object> item : items) {
                Object nameObj = item.get("productName");
                if (nameObj != null) {
                    productNames.append(nameObj.toString()).append(", ");
                }
            }

            // Remove trailing comma
            if (productNames.length() > 2) {
                productNames.setLength(productNames.length() - 2);
            }

            holder.binding.productsTxt.setText("Products: " + productNames);
        } else {
            holder.binding.productsTxt.setText("Products: N/A");
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        OrderItemUserBinding binding;

        public ViewHolder(@NonNull OrderItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
