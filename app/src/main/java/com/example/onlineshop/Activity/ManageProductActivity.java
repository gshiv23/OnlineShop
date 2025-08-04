package com.example.onlineshop.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineshop.Adapter.ManageProductAdapter;
import com.example.onlineshop.Domain.CategoryDomain;
import com.example.onlineshop.Domain.ProductDomain;
import com.example.onlineshop.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class ManageProductActivity extends AppCompatActivity {

    private RecyclerView recyclerManageProduct;
    private FloatingActionButton btnAddProduct;
    private List<ProductDomain> productList = new ArrayList<>();
    private ManageProductAdapter adapter;
    private DatabaseReference productRef, categoryRef;

    private List<CategoryDomain> categoryList = new ArrayList<>();
    private List<String> categoryNames = new ArrayList<>();
    private ArrayAdapter<String> adapterSpinner;

    private ImageView imagePreview;
    private String encodedImage = "";

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_product);

        recyclerManageProduct = findViewById(R.id.recyclerManageProduct);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        recyclerManageProduct.setLayoutManager(new LinearLayoutManager(this));

        productRef = FirebaseDatabase.getInstance().getReference("Products");
        categoryRef = FirebaseDatabase.getInstance().getReference("Categories");

        adapter = new ManageProductAdapter(
                this,
                (ArrayList<ProductDomain>) productList,
                this::showEditDialog,
                this::deleteProduct
        );
        recyclerManageProduct.setAdapter(adapter);

        btnAddProduct.setOnClickListener(v -> {
            if (categoryList.isEmpty()) {
                Toast.makeText(this, "Please wait, categories are still loading...", Toast.LENGTH_SHORT).show();
                return;
            }
            showProductDialog(null);
        });

        loadCategories(); // important to call before opening dialog
        loadProducts();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            imagePreview.setImageBitmap(bitmap);
                            encodedImage = bitmapToBase64(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void loadProducts() {
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ProductDomain product = data.getValue(ProductDomain.class);
                    if (product != null) {
                        product.setId(data.getKey());
                        productList.add(product);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageProductActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories() {
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                categoryNames.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    CategoryDomain category = data.getValue(CategoryDomain.class);
                    if (category != null) {
                        category.setId(data.getKey());
                        categoryList.add(category);
                        categoryNames.add(category.getName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageProductActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog(ProductDomain product) {
        if (categoryList.isEmpty()) {
            Toast.makeText(this, "Please wait, categories are still loading...", Toast.LENGTH_SHORT).show();
            return;
        }
        showProductDialog(product);
    }

    private void showProductDialog(ProductDomain productToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        EditText editTitle = view.findViewById(R.id.etTitle);
        EditText editReview = view.findViewById(R.id.etReview);
        EditText editScore = view.findViewById(R.id.etScore);
        EditText editPrice = view.findViewById(R.id.etPrice);
        EditText editDescription = view.findViewById(R.id.etDescription);
        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        imagePreview = view.findViewById(R.id.ivProductImage);
        Button btnUploadImage = view.findViewById(R.id.btnUploadImage);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryNames);
        spinnerCategory.setAdapter(adapterSpinner);

        if (productToEdit != null) {
            editTitle.setText(productToEdit.getTitle());
            editReview.setText(productToEdit.getReview());
            editScore.setText(String.valueOf(productToEdit.getScore()));
            editPrice.setText(String.valueOf(productToEdit.getPrice()));
            editDescription.setText(productToEdit.getDescription());
            encodedImage = productToEdit.getPic();

            if (!TextUtils.isEmpty(encodedImage)) {
                byte[] imageBytes = Base64.decode(encodedImage, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imagePreview.setImageBitmap(decodedBitmap);
            }

            for (int i = 0; i < categoryList.size(); i++) {
                if (categoryList.get(i).getId().equals(productToEdit.getCategory())) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }

        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String review = editReview.getText().toString().trim();
            String scoreStr = editScore.getText().toString().trim();
            String priceStr = editPrice.getText().toString().trim();
            String description = editDescription.getText().toString().trim();
            int selectedIndex = spinnerCategory.getSelectedItemPosition();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(encodedImage)) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedIndex < 0 || selectedIndex >= categoryList.size()) {
                Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedCategoryId = categoryList.get(selectedIndex).getId();
            double score = 0;
            double price;

            try {
                score = TextUtils.isEmpty(scoreStr) ? 0.0 : Double.parseDouble(scoreStr);
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number input", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, Object> map = new HashMap<>();
            map.put("title", title);
            map.put("pic", encodedImage);
            map.put("review", review);
            map.put("score", score);
            map.put("price", price);
            map.put("description", description);
            map.put("category", selectedCategoryId);

            if (productToEdit == null) {
                productRef.push().setValue(map)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show());
            } else {
                productRef.child(productToEdit.getId()).updateChildren(map)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to update product", Toast.LENGTH_SHORT).show());
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void deleteProduct(ProductDomain product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    productRef.child(product.getId()).removeValue()
                            .addOnSuccessListener(unused -> Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show();
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
