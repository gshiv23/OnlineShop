package com.example.onlineshop.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.onlineshop.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuIcon = findViewById(R.id.menu_icon);

        // ✅ Open Drawer on Menu Click
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // ✅ Navigation Item Clicks (Fixed: using if-else to avoid "Constant expression required")
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_manage_category) {
                startActivity(new Intent(AdminDashboardActivity.this, ManageCategoryActivity.class));
            } else if (id == R.id.nav_manage_product) {
                startActivity(new Intent(AdminDashboardActivity.this, ManageProductActivity.class));
            } else if (id == R.id.nav_manage_orders) {
                // Pass isAdmin as true (since you are in admin dashboard)
                Intent intent = new Intent(AdminDashboardActivity.this, ManageOrdersActivity.class);
                intent.putExtra("isAdmin", true);
                startActivity(intent);
            } else if (id == R.id.nav_logout) {
                logoutAdmin();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void logoutAdmin() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        FirebaseAuth.getInstance().signOut();

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        // ✅ Close drawer if open on back press
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
