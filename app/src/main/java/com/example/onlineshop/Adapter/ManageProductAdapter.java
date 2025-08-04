package com.example.onlineshop.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineshop.Domain.ProductDomain;
import com.example.onlineshop.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.database.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class ManageProductAdapter extends RecyclerView.Adapter<ManageProductAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ProductDomain> productList;
    private EditProductCallback editCallback;
    private DeleteProductCallback deleteCallback;

    public ManageProductAdapter(Context context, ArrayList<ProductDomain> productList,
                                EditProductCallback editCallback,
                                DeleteProductCallback deleteCallback) {
        this.context = context;
        this.productList = productList;
        this.editCallback = editCallback;
        this.deleteCallback = deleteCallback;
    }

    public interface EditProductCallback {
        void onEdit(ProductDomain product);
    }

    public interface DeleteProductCallback {
        void onDelete(ProductDomain product);
    }

    @NonNull
    @Override
    public ManageProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageProductAdapter.ViewHolder holder, int position) {
        final ProductDomain product = productList.get(position);
        final int currentPosition = position;
        holder.bind(product, currentPosition);
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageButton editBtn, deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            editBtn = itemView.findViewById(R.id.btnEdit);
            deleteBtn = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(final ProductDomain product, final int position) {
            editBtn.setOnClickListener(v -> showEditPopup(product, position));
            deleteBtn.setOnClickListener(v -> confirmDelete(product, position));
        }


        private void showEditPopup(ProductDomain product, int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_product, null);
            builder.setView(view);

            EditText etTitle = view.findViewById(R.id.etTitle);
            EditText etReview = view.findViewById(R.id.etReview);
            EditText etScore = view.findViewById(R.id.etScore);
            EditText etPrice = view.findViewById(R.id.etPrice);
            EditText etDescription = view.findViewById(R.id.etDescription);
            ImageView ivProductImage = view.findViewById(R.id.ivProductImage);
            Button btnUploadImage = view.findViewById(R.id.btnUploadImage);
            Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
            Button btnSave = view.findViewById(R.id.btnSave);
            Button btnCancel = view.findViewById(R.id.btnCancel);

            etTitle.setText(product.getTitle());
            etReview.setText(product.getReview());
            etScore.setText(String.valueOf(product.getScore()));
            etPrice.setText(String.valueOf(product.getPrice()));
            etDescription.setText(product.getDescription());

            if (product.getImageBase64() != null && !product.getImageBase64().isEmpty()) {
                byte[] decodedBytes = Base64.decode(product.getImageBase64(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                ivProductImage.setImageBitmap(bitmap);
            }

            List<String> categoryList = new ArrayList<>();
            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categoryList);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(categoryAdapter);

            DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("Category");
            categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    categoryList.clear();
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        String title = snap.child("title").getValue(String.class);
                        if (title != null) categoryList.add(title);
                    }
                    categoryAdapter.notifyDataSetChanged();

                    int index = categoryList.indexOf(product.getCategory());
                    if (index != -1) spinnerCategory.setSelection(index);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            });

            final Bitmap[] selectedBitmap = {null};
            final String[] imageBase64Holder = {product.getImageBase64()};

            btnUploadImage.setOnClickListener(v -> {
                ImagePicker.with((Activity) context)
                        .crop()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start(); // handled in activity result
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            btnSave.setOnClickListener(v -> {
                String title = etTitle.getText().toString().trim();
                String review = etReview.getText().toString().trim();
                String scoreStr = etScore.getText().toString().trim();
                String priceStr = etPrice.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                String category = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "";

                if (title.isEmpty() || scoreStr.isEmpty() || priceStr.isEmpty() || category.isEmpty()) {
                    Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                double score, price;
                try {
                    score = Double.parseDouble(scoreStr);
                    price = Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Enter valid numbers for score and price", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update imageBase64 if new image was selected
                if (selectedBitmap[0] != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    selectedBitmap[0].compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] imageBytes = baos.toByteArray();
                    imageBase64Holder[0] = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                }

                Map<String, Object> updatedData = new HashMap<>();
                updatedData.put("title", title);
                updatedData.put("review", review);
                updatedData.put("score", score);
                updatedData.put("price", price);
                updatedData.put("description", description);
                updatedData.put("category", category);
                updatedData.put("pic", imageBase64Holder[0]);

                FirebaseDatabase.getInstance().getReference("Product")
                        .child(product.getId())
                        .updateChildren(updatedData)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(context, "Product updated", Toast.LENGTH_SHORT).show();
                            productList.set(position, new ProductDomain(
                                    product.getId(), title, imageBase64Holder[0], review, score, price, description, category
                            ));
                            notifyItemChanged(position);
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show());
            });

            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        private void confirmDelete(ProductDomain product, int position) {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Product")
                    .setMessage("Are you sure you want to delete this product?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        FirebaseDatabase.getInstance().getReference("Product")
                                .child(product.getId())
                                .removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    productList.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Deletion failed", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
}
