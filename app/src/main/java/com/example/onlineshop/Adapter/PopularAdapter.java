package com.example.onlineshop.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.example.onlineshop.Activity.DetailActivity;
import com.example.onlineshop.databinding.ViewholderPupListBinding;
import com.example.onlineshop.Domain.PopularDomain;

import java.util.ArrayList;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.Viewholder> {

    private final ArrayList<PopularDomain> items;

    public PopularAdapter(ArrayList<PopularDomain> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderPupListBinding binding = ViewholderPupListBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        PopularDomain item = items.get(position);

        // ✅ Set Texts (with null safety)
        holder.binding.titleTxt.setText(item.getTitle() != null ? item.getTitle() : "No Title");
        holder.binding.feeTxt.setText("Rs." + item.getPrice());
        holder.binding.scoreTxt.setText(String.valueOf(item.getScore()));
        holder.binding.reviewTxt.setText(String.valueOf(item.getReview()));

        // ✅ Load Image (supports both drawable names & URLs)
        String pic = item.getPic();
        if (pic != null && !pic.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(pic.startsWith("http") ? pic :
                            holder.itemView.getResources().getIdentifier(
                                    pic, "drawable", holder.itemView.getContext().getPackageName()))
                    .placeholder(android.R.color.darker_gray)
                    .transform(new GranularRoundedCorners(30, 30, 0, 0))
                    .into(holder.binding.imageView15);
        } else {
            holder.binding.imageView15.setImageResource(android.R.color.darker_gray);
        }

        // ✅ OnClick → Open DetailActivity with full object
        holder.itemView.setOnClickListener(view -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(view.getContext(), DetailActivity.class);
                intent.putExtra("object", items.get(currentPos));
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ✅ Update list for filtering/searching
    public void updateList(ArrayList<PopularDomain> newList) {
        items.clear();
        items.addAll(newList);
        notifyDataSetChanged();
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        ViewholderPupListBinding binding;

        public Viewholder(ViewholderPupListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
