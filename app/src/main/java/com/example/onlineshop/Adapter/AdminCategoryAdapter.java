package com.example.onlineshop.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.onlineshop.Activity.ManageCategoryActivity;
import com.example.onlineshop.Model.CategoryModel;
import com.example.onlineshop.R;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder> {

    private Context context;
    private List<CategoryModel> list;
    private DatabaseReference categoryRef;

    public AdminCategoryAdapter(Context context, List<CategoryModel> list, DatabaseReference categoryRef) {
        this.context = context;
        this.list = list;
        this.categoryRef = categoryRef;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryModel category = list.get(position);

        holder.tvCategoryName.setText(category.getName());

        if (category.getImage() != null && !category.getImage().isEmpty()) {
            Bitmap bitmap = ManageCategoryActivity.decodeImage(category.getImage());

            // ✅ Load Circular Cropped Image using Glide
            Glide.with(holder.itemView.getContext())
                    .load(bitmap)
                    .placeholder(R.drawable.ic_add_image)
                    .transform(new com.bumptech.glide.load.resource.bitmap.CircleCrop())
                    .into(holder.ivCategoryImage);
        } else {
            holder.ivCategoryImage.setImageResource(R.drawable.ic_add_image);
        }

        // ✅ Edit on Click
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof ManageCategoryActivity) {
                ((ManageCategoryActivity) context).showEditCategoryDialog(category);
            }
        });

        // ✅ Delete on Long Click
        holder.itemView.setOnLongClickListener(v -> {
            if (context instanceof ManageCategoryActivity) {
                ((ManageCategoryActivity) context).deleteCategory(category);
            }
            return true;
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
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
