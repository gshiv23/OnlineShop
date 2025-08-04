package com.example.onlineshop.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.onlineshop.Domain.PopularDomain;
import com.example.onlineshop.R;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<PopularDomain> wishlistList;
    private final DatabaseReference wishlistRef;

    public WishlistAdapter(Context context, ArrayList<PopularDomain> wishlistList, DatabaseReference wishlistRef) {
        this.context = context;
        this.wishlistList = wishlistList;
        this.wishlistRef = wishlistRef;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.wishlist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PopularDomain product = wishlistList.get(position);

        holder.title.setText(product.getTitle());
        holder.price.setText("Rs. " + product.getPrice());

        // ✅ Load image (URL, Base64, or drawable)
        loadProductImage(holder.pic, product.getPic());

        // ✅ Remove from wishlist by ID (not order-based)
        holder.removeBtn.setOnClickListener(v -> {
            if (product.getId() != null) {
                wishlistRef.child(product.getId()).removeValue()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(context, "Removed from Wishlist", Toast.LENGTH_SHORT).show();

                            // ✅ Remove the product by its ID (not relying on position)
                            removeItemById(product.getId());
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Failed to remove", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public int getItemCount() {
        return wishlistList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView pic;
        TextView title, price;
        ImageButton removeBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pic = itemView.findViewById(R.id.wishlist_image);
            title = itemView.findViewById(R.id.wishlist_title);
            price = itemView.findViewById(R.id.wishlist_price);
            removeBtn = itemView.findViewById(R.id.remove_wishlist_btn);
        }
    }

    /**
     * ✅ Remove item from local list by its ID (independent of order)
     */
    private void removeItemById(String productId) {
        int indexToRemove = -1;

        for (int i = 0; i < wishlistList.size(); i++) {
            if (wishlistList.get(i).getId().equals(productId)) {
                indexToRemove = i;
                break;
            }
        }

        if (indexToRemove != -1) {
            wishlistList.remove(indexToRemove);
            notifyItemRemoved(indexToRemove);
            notifyItemRangeChanged(indexToRemove, wishlistList.size());
        }
    }

    /**
     * ✅ Universal Image Loader for Wishlist
     */
    private void loadProductImage(ImageView imageView, String pic) {
        if (pic == null || pic.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_placeholder);
            return;
        }

        if (pic.startsWith("http")) {
            // ✅ URL image
            Glide.with(context)
                    .load(pic)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(imageView);

        } else if (pic.length() > 100) {
            // ✅ Base64 encoded image
            try {
                byte[] decodedBytes = Base64.decode(pic, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.ic_placeholder);
            }

        } else {
            // ✅ Drawable resource name (TinyDB stored string)
            int drawableRes = context.getResources().getIdentifier(pic, "drawable", context.getPackageName());
            Glide.with(context)
                    .load(drawableRes != 0 ? drawableRes : R.drawable.ic_placeholder)
                    .into(imageView);
        }
    }
}
