package com.example.onlineshop.Activity;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineshop.Adapter.AdminCategoryAdapter;
import com.example.onlineshop.Model.CategoryModel;
import com.example.onlineshop.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ManageCategoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private DatabaseReference categoryRef;
    private List<CategoryModel> categoryList;
    private AdminCategoryAdapter adapter;

    private Bitmap selectedBitmap = null;
    private ImageView currentDialogImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_category);

        recyclerView = findViewById(R.id.recycler_categories);
        fabAdd = findViewById(R.id.fab_add_category);

        categoryRef = FirebaseDatabase.getInstance().getReference("Categories");
        categoryList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminCategoryAdapter(this, categoryList, categoryRef);
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showAddCategoryDialog());
        loadCategories();
    }

    private void loadCategories() {
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    CategoryModel category = dataSnapshot.getValue(CategoryModel.class);
                    if (category != null) {
                        categoryList.add(category);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageCategoryActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddCategoryDialog() {
        selectedBitmap = null;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText etCategoryName = view.findViewById(R.id.et_category_name);
        currentDialogImageView = view.findViewById(R.id.iv_category_image);

        currentDialogImageView.setOnClickListener(v -> openImagePicker());

        builder.setView(view)
                .setTitle("Add Category")
                .setPositiveButton("Save", (dialog, which) -> {
                    String categoryName = etCategoryName.getText().toString().trim();
                    if (!TextUtils.isEmpty(categoryName) && selectedBitmap != null) {
                        addCategory(categoryName, selectedBitmap);
                    } else {
                        Toast.makeText(this, "Enter name & select image", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        if (currentDialogImageView != null) {
                            currentDialogImageView.setImageBitmap(selectedBitmap);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void addCategory(String name, Bitmap bitmap) {
        String id = UUID.randomUUID().toString();
        String imageBase64 = encodeImage(bitmap);

        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("image", imageBase64);

        categoryRef.child(id).setValue(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Category added successfully", Toast.LENGTH_SHORT).show();
                selectedBitmap = null;
            } else {
                Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Edit Category
    public void showEditCategoryDialog(CategoryModel category) {
        selectedBitmap = decodeImage(category.getImage());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText etCategoryName = view.findViewById(R.id.et_category_name);
        currentDialogImageView = view.findViewById(R.id.iv_category_image);

        etCategoryName.setText(category.getName());
        currentDialogImageView.setImageBitmap(selectedBitmap);

        currentDialogImageView.setOnClickListener(v -> openImagePicker());

        builder.setView(view)
                .setTitle("Edit Category")
                .setPositiveButton("Update", (dialog, which) -> {
                    String updatedName = etCategoryName.getText().toString().trim();
                    if (!TextUtils.isEmpty(updatedName) && selectedBitmap != null) {
                        updateCategory(category.getId(), updatedName, selectedBitmap);
                    } else {
                        Toast.makeText(this, "Enter name & select image", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    private void updateCategory(String id, String name, Bitmap bitmap) {
        String imageBase64 = encodeImage(bitmap);
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("image", imageBase64);

        categoryRef.child(id).updateChildren(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Category updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Delete Category
    public void deleteCategory(CategoryModel category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    categoryRef.child(category.getId()).removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    public static String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public static Bitmap decodeImage(String base64) {
        byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
    }
}
