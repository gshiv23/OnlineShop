package com.example.onlineshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineshop.Adapter.CartAdapter;
import com.example.onlineshop.Helper.ManagmentCart;
import com.example.onlineshop.R;
import com.example.onlineshop.databinding.ActivityCartBinding;
import com.example.onlineshop.Domain.PopularDomain;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class CartActivity extends AppCompatActivity implements PaymentResultListener {

    private ManagmentCart managmentCart;
    private ActivityCartBinding binding;
    private double tax;
    private FirebaseUser currentUser;
    private ArrayList<PopularDomain> currentCartItems = new ArrayList<>();

    private DatabaseReference userRef;
    private String userAddress = null;
    private String userPhone = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please login to access your cart.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        managmentCart = new ManagmentCart(this);
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        updateStatusBarColor();
        setListeners();
        setupCartList();
        loadUserAddressFromFirebase();
    }

    private void updateStatusBarColor() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blue_Dark));
    }

    private void setListeners() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.imageView20.setOnClickListener(v ->
                startActivity(new Intent(this, AddressActivity.class)));

        binding.orderNowBtn.setOnClickListener(v -> {
            if (currentCartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty.", Toast.LENGTH_SHORT).show();
            } else if (userAddress == null || userAddress.isEmpty()) {
                Toast.makeText(this, "Please add a delivery address.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, AddressActivity.class));
            } else {
                startPayment();
            }
        });
    }

    private void setupCartList() {
        managmentCart.getListCart(cartItems -> {
            currentCartItems = cartItems;

            if (cartItems.isEmpty()) {
                binding.emptyTxt.setVisibility(android.view.View.VISIBLE);
                binding.scroll.setVisibility(android.view.View.GONE);
            } else {
                binding.emptyTxt.setVisibility(android.view.View.GONE);
                binding.scroll.setVisibility(android.view.View.VISIBLE);

                binding.cartView.setLayoutManager(new LinearLayoutManager(this));
                binding.cartView.setAdapter(new CartAdapter(cartItems, this::calculateCartTotals, managmentCart));
            }

            calculateCartTotals();
        });
    }

    private void calculateCartTotals() {
        managmentCart.getTotalFee(subtotal -> {
            double percentTax = 0.02;
            double delivery = 10.0;

            tax = Math.round(subtotal * percentTax * 100.0) / 100.0;
            double total = Math.round((subtotal + tax + delivery) * 100.0) / 100.0;

            binding.totalfeeTxt.setText("Rs." + String.format("%.2f", subtotal));
            binding.taxTxt.setText("Rs." + String.format("%.2f", tax));
            binding.deliveryTxt.setText("Rs." + String.format("%.2f", delivery));
            binding.totalTxt.setText("Rs." + String.format("%.2f", total));
        });
    }

    /** ✅ Load Address & Phone from Firebase **/
    private void loadUserAddressFromFirebase() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userAddress = snapshot.child("address").getValue(String.class);
                    userPhone = snapshot.child("phoneNumber").getValue(String.class);

                    if (userAddress != null && !userAddress.isEmpty()) {
                        binding.textView34.setText(userAddress);
                    } else {
                        binding.textView34.setText("No address added");
                    }
                } else {
                    binding.textView34.setText("No address added");
                    userAddress = null;
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(CartActivity.this, "Failed to load address", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startPayment() {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_5sAQLcdlVbZPl8");

        double subtotal = 0;
        for (PopularDomain item : currentCartItems) {
            subtotal += item.getPrice() * item.getNumberInCart();
        }
        double amountToPay = Math.round((subtotal + tax + 10.0) * 100);

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Online Shop");
            options.put("description", "Order Payment");
            options.put("theme.color", "#1976D2");
            options.put("currency", "INR");
            options.put("amount", amountToPay);

            if (currentUser.getEmail() != null)
                options.put("prefill.email", currentUser.getEmail());
            options.put("prefill.contact", userPhone != null ? userPhone : "+919510107650");

            checkout.open(this, options);
        } catch (Exception e) {
            Toast.makeText(this, "Payment error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        Toast.makeText(this, "Payment Successful: " + razorpayPaymentID, Toast.LENGTH_SHORT).show();

        double subtotal = 0;
        for (PopularDomain item : currentCartItems) {
            subtotal += item.getPrice() * item.getNumberInCart();
        }
        subtotal = Math.round(subtotal * 100.0) / 100.0;
        double delivery = 10.0;
        double totalAmount = Math.round((subtotal + tax + delivery) * 100.0) / 100.0;

        saveOrderToFirebase(razorpayPaymentID, subtotal, tax, delivery, totalAmount);

        managmentCart.clearCart();

        Intent intent = new Intent(this, ReceiptActivity.class);
        intent.putExtra("payment_id", razorpayPaymentID);
        intent.putExtra("user_address", userAddress != null ? userAddress : "No address");
        intent.putExtra("subtotal", subtotal);
        intent.putExtra("tax", tax);
        intent.putExtra("delivery", delivery);
        intent.putExtra("total_amount", totalAmount);
        startActivity(intent);
        finish();
    }

    /** ✅ Save Order to Firebase **/
    private void saveOrderToFirebase(String paymentId, double subtotal, double tax, double delivery, double totalAmount) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        String userId = currentUser.getUid();
        String orderId = ordersRef.push().getKey(); // Save under Orders/orderId (not nested under userId)

        if (orderId == null) {
            Toast.makeText(this, "Failed to generate order ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();

        ArrayList<HashMap<String, Object>> itemsList = new ArrayList<>();
        for (PopularDomain item : currentCartItems) {
            HashMap<String, Object> product = new HashMap<>();
            product.put("productName", item.getTitle());
            product.put("price", item.getPrice());
            product.put("quantity", item.getNumberInCart());
            product.put("imageUrl", item.getPic());
            itemsList.add(product);
        }

        HashMap<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("paymentId", paymentId);
        orderData.put("userId", userId); // 🔑 Important for permission rules
        orderData.put("address", userAddress != null ? userAddress : "No address");
        orderData.put("phone", userPhone != null ? userPhone : "Not available");
        orderData.put("subtotal", subtotal);
        orderData.put("tax", tax);
        orderData.put("delivery", delivery);
        orderData.put("totalAmount", totalAmount);
        orderData.put("timestamp", timestamp);
        orderData.put("status", "Pending");
        orderData.put("items", itemsList);

        ordersRef.child(orderId).setValue(orderData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Order saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save order: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }



    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(this, "Payment Failed: " + response, Toast.LENGTH_SHORT).show();
    }
}
