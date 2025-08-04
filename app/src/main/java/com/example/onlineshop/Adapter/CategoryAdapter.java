package com.example.onlineshop.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.onlineshop.Domain.CategoryDomain;
import com.example.onlineshop.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<CategoryDomain> list;
    private OnCategoryClickListener onCategoryClickListener;

    public CategoryAdapter(List<CategoryDomain> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryDomain category = list.get(position);
        holder.tvCategoryName.setText(category.getName());

        // ✅ Load Image (URL or Base64) - Circular Crop
        if (category.getImage() != null && !category.getImage().isEmpty()) {
            if (category.getImage().startsWith("http")) {
                // URL Image
                Glide.with(holder.itemView.getContext())
                        .load(category.getImage())
                        .placeholder(R.drawable.ic_add_image)
                        .transform(new CircleCrop()) // ✅ Circular Crop
                        .into(holder.ivCategoryImage);
            } else {
                // Base64 Image
                try {
                    byte[] decoded = Base64.decode(category.getImage(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);

                    Glide.with(holder.itemView.getContext())
                            .load(bitmap)
                            .placeholder(R.drawable.ic_add_image)
                            .transform(new CircleCrop()) // ✅ Circular Crop
                            .into(holder.ivCategoryImage);
                } catch (Exception e) {
                    holder.ivCategoryImage.setImageResource(R.drawable.ic_add_image);
                }
            }
        } else {
            holder.ivCategoryImage.setImageResource(R.drawable.ic_add_image);
        }

        // ✅ Clickable Categories (Open product list or details)
        holder.itemView.setOnClickListener(v -> {
            if (onCategoryClickListener != null) {
                onCategoryClickListener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ✅ Click Listener
    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryDomain category);
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.onCategoryClickListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        ImageView ivCategoryImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            ivCategoryImage = itemView.findViewById(R.id.iv_category_image);
        }
    }
}
