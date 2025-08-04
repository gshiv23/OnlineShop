package com.example.onlineshop.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.onlineshop.Domain.PopularDomain;
import com.example.onlineshop.Helper.ManagmentCart;
import com.example.onlineshop.R;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final ArrayList<PopularDomain> list;
    private final ManagmentCart managmentCart;
    private final ChangeNumberItemsListener changeNumberItemsListener;

    public interface ChangeNumberItemsListener {
        void changed();
    }

    public CartAdapter(ArrayList<PopularDomain> list, ChangeNumberItemsListener changeNumberItemsListener, ManagmentCart managmentCart) {
        this.list = list;
        this.changeNumberItemsListener = changeNumberItemsListener;
        this.managmentCart = managmentCart;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PopularDomain item = list.get(position);

        holder.title.setText(item.getTitle());
        holder.feeEachItem.setText("Rs. " + item.getPrice());
        holder.totalEachItem.setText("Rs. " + Math.round((item.getNumberInCart() * item.getPrice()) * 100) / 100.0);
        holder.num.setText(String.valueOf(item.getNumberInCart()));

        // ✅ Load Image (Firebase URL or Drawable)
        String pic = item.getPic();
        if (pic != null && !pic.isEmpty()) {
            if (pic.startsWith("http")) {
                Glide.with(holder.itemView.getContext())
                        .load(pic)
                        .placeholder(R.drawable.ic_placeholder)
                        .transform(new RoundedCorners(20))
                        .into(holder.pic);
            } else {
                int drawableRes = holder.itemView.getResources()
                        .getIdentifier(pic, "drawable", holder.itemView.getContext().getPackageName());
                Glide.with(holder.itemView.getContext())
                        .load(drawableRes != 0 ? drawableRes : R.drawable.ic_placeholder)
                        .transform(new RoundedCorners(20))
                        .into(holder.pic);
            }
        } else {
            holder.pic.setImageResource(R.drawable.ic_placeholder);
        }

        // ✅ Plus Button (Corrected)
        holder.plusItem.setOnClickListener(v -> {
            int newQty = item.getNumberInCart() + 1;
            managmentCart.plusNumberItem(item, newQty, () -> {
                notifyItemChanged(position);
                changeNumberItemsListener.changed();
            });
        });

        // ✅ Minus Button (Corrected)
        holder.minusItem.setOnClickListener(v -> {
            int newQty = item.getNumberInCart() - 1;
            managmentCart.minusNumberItem(item, newQty, () -> {
                notifyItemChanged(position);
                changeNumberItemsListener.changed();
            });
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, feeEachItem, totalEachItem, num;
        ImageView pic;
        TextView plusItem, minusItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleTxt);
            feeEachItem = itemView.findViewById(R.id.feeEachItem);
            totalEachItem = itemView.findViewById(R.id.totalEachItem);
            num = itemView.findViewById(R.id.numberItemTxt);
            pic = itemView.findViewById(R.id.pic);
            plusItem = itemView.findViewById(R.id.plusCartBtn);
            minusItem = itemView.findViewById(R.id.minusCartBtn);
        }
    }
}
