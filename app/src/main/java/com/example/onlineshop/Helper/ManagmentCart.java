package com.example.onlineshop.Helper;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.onlineshop.Domain.PopularDomain;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class ManagmentCart {
    private Context context;
    private DatabaseReference cartRef;
    private String userId;

    public ManagmentCart(Context context) {
        this.context = context;
        userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "guest";

        cartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId);
    }

    // ✅ Add or Update Cart Item
    public void insertFood(PopularDomain item) {
        String key = item.getTitle(); // Use title as key (you can also use item ID)

        cartRef.child(key).setValue(item)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ✅ Get All Cart Items
    public void getListCart(FirebaseCartCallback callback) {
        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<PopularDomain> cartItems = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    PopularDomain item = snap.getValue(PopularDomain.class);
                    if (item != null) cartItems.add(item);
                }
                callback.onCallback(cartItems);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCallback(new ArrayList<>()); // Return empty list on failure
            }
        });
    }

    // ✅ Increase Quantity
    public void plusNumberItem(PopularDomain item, int newQty, ChangeNumberItemsListener listener) {
        item.setNumberInCart(newQty);
        cartRef.child(item.getTitle()).setValue(item)
                .addOnSuccessListener(aVoid -> listener.change());
    }

    // ✅ Decrease Quantity
    public void minusNumberItem(PopularDomain item, int newQty, ChangeNumberItemsListener listener) {
        if (newQty <= 0) {
            cartRef.child(item.getTitle()).removeValue().addOnSuccessListener(aVoid -> listener.change());
        } else {
            item.setNumberInCart(newQty);
            cartRef.child(item.getTitle()).setValue(item).addOnSuccessListener(aVoid -> listener.change());
        }
    }

    // ✅ Calculate Total Fee
    public void getTotalFee(FirebaseTotalCallback callback) {
        getListCart(cartItems -> {
            double total = 0;
            for (PopularDomain item : cartItems) {
                total += item.getPrice() * item.getNumberInCart();
            }
            callback.onCallback(total);
        });
    }

    // ✅ Clear Cart After Payment
    public void clearCart() {
        cartRef.removeValue();
    }

    // ✅ Callback Interfaces
    public interface FirebaseCartCallback {
        void onCallback(ArrayList<PopularDomain> cartItems);
    }

    public interface FirebaseTotalCallback {
        void onCallback(double total);
    }
}
