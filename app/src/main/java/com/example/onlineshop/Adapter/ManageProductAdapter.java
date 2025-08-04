package com.example.onlineshop.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineshop.Domain.ProductDomain;
import com.example.onlineshop.R;

import android.util.Base64;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

import java.util.ArrayList;

public class ManageProductAdapter extends RecyclerView.Adapter<ManageProductAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ProductDomain> productList;
    private OnProductClickListener editListener, deleteListener;

    public interface OnProductClickListener {
        void onClick(ProductDomain product);
    }

    public ManageProductAdapter(Context context, ArrayList<ProductDomain> productList,
                                OnProductClickListener editListener,
                                OnProductClickListener deleteListener) {
        this.context = context;
        this.productList = productList;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductDomain product = productList.get(position);
        holder.title.setText(product.getTitle());
        holder.price.setText("₹" + product.getPrice());

        if (product.getPic() != null && !product.getPic().isEmpty()) {
            byte[] bytes = Base64.decode(product.getPic(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            holder.image.setImageBitmap(bitmap);
        }

        holder.btnEdit.setOnClickListener(v -> editListener.onClick(product));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onClick(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, price;
        ImageView image;
        Button btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvProductTitle);
            price = itemView.findViewById(R.id.tvProductPrice);
            image = itemView.findViewById(R.id.ivProduct);
            btnEdit = itemView.findViewById(R.id.btnEditProduct);
            btnDelete = itemView.findViewById(R.id.btnDeleteProduct);
        }
    }
}
