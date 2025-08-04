package com.example.onlineshop.Activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.onlineshop.R;
import com.example.onlineshop.databinding.ActivityReceiptBinding;
import com.example.onlineshop.Domain.PopularDomain;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReceiptActivity extends AppCompatActivity {

    private ActivityReceiptBinding binding;
    private ArrayList<PopularDomain> purchasedItems;
    private String paymentId;
    private String address;
    private double subtotal, tax, delivery, totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiptBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setStatusBarColor();
        loadIntentData();
        setupListeners();
        displayData();
    }

    private void setStatusBarColor() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blue_Dark));
    }

    private void setupListeners() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        binding.btnDownloadReceipt.setOnClickListener(v -> {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        });
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        paymentId = intent.getStringExtra("payment_id");
        address = intent.getStringExtra("user_address");
        subtotal = intent.getDoubleExtra("subtotal", 0.0);
        tax = intent.getDoubleExtra("tax", 0.0);
        delivery = intent.getDoubleExtra("delivery", 0.0);
        totalAmount = intent.getDoubleExtra("total_amount", 0.0);

        String cartJson = intent.getStringExtra("purchased_products");
        if (cartJson != null) {
            Type type = new TypeToken<ArrayList<PopularDomain>>() {}.getType();
            purchasedItems = new Gson().fromJson(cartJson, type);
        } else {
            purchasedItems = new ArrayList<>();
        }
    }

    private void displayData() {
        binding.paymentId.setText("Payment ID: " + (paymentId != null ? paymentId : "Unavailable"));
        binding.address.setText("Delivery Address: " + (address != null ? address : "Not Provided"));
        binding.subtotal.setText("Subtotal: ₹" + format(subtotal));
        binding.tax.setText("Tax: ₹" + format(tax));
        binding.delivery.setText("Delivery: ₹" + format(delivery));
        binding.total.setText("Total Paid: ₹" + format(totalAmount));
    }

    private String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private void generateReceiptPDF() {
        PdfDocument document = new PdfDocument();
        Paint paint = new Paint();
        Paint borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);
        borderPaint.setColor(Color.BLACK);

        int pageWidth = 595, pageHeight = 842; // A4
        int y = 60, x = 20;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Draw border rectangle
        canvas.drawRect(10, 10, pageWidth - 10, pageHeight - 10, borderPaint);

        // Header
        paint.setTextSize(22);
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(true);
        canvas.drawText("Online Shop - Payment Receipt", x + 90, y, paint);
        y += 40;

        paint.setTextSize(14);
        paint.setFakeBoldText(false);
        canvas.drawText("Date: " + new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()), x, y, paint);
        y += 30;

        // Info Section
        canvas.drawRect(x, y - 20, pageWidth - x, y + 140, borderPaint);
        canvas.drawText("Payment ID: " + paymentId, x + 10, y + 20, paint);
        canvas.drawText("Delivery Address: " + address, x + 10, y + 50, paint);
        canvas.drawText("Subtotal: ₹" + format(subtotal), x + 10, y + 80, paint);
        canvas.drawText("Tax: ₹" + format(tax), x + 10, y + 100, paint);
        canvas.drawText("Delivery: ₹" + format(delivery), x + 10, y + 120, paint);
        canvas.drawText("Total Paid: ₹" + format(totalAmount), x + 10, y + 140, paint);
        y += 160;

        // QR Code Generation
        try {
            String qrData = "PaymentID: " + paymentId + "\nTotal: ₹" + format(totalAmount);
            Bitmap qrBitmap = new BarcodeEncoder().encodeBitmap(qrData, BarcodeFormat.QR_CODE, 150, 150);
            canvas.drawBitmap(qrBitmap, pageWidth - 170, y - 10, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Footer
        paint.setTextSize(12);
        canvas.drawText("Thank you for shopping with us!", x + 10, pageHeight - 40, paint);

        document.finishPage(page);

        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, "receipt_" + System.currentTimeMillis() + ".pdf");
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            fos.close();
            Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        document.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            generateReceiptPDF();
        }
    }
}
