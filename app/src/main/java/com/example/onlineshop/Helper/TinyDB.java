package com.example.onlineshop.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.example.onlineshop.Domain.DeliveryAddress;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class TinyDB {
    private SharedPreferences preferences;
    private String DEFAULT_APP_IMAGEDATA_DIRECTORY;
    private String lastImagePath = "";

    public TinyDB(Context appContext) {
        preferences = appContext.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
    }

    // ✅ Image Handling (Profile Pictures / Cached Images)
    public Bitmap getImage(String path) {
        try {
            return BitmapFactory.decodeFile(path);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String putImage(String folder, String imageName, Bitmap bitmap) {
        if (folder == null || imageName == null || bitmap == null) return null;
        this.DEFAULT_APP_IMAGEDATA_DIRECTORY = folder;
        String fullPath = setupFullPath(imageName);

        if (!fullPath.isEmpty()) {
            lastImagePath = fullPath;
            saveBitmap(fullPath, bitmap);
        }
        return fullPath;
    }

    private String setupFullPath(String imageName) {
        File mFolder = new File(Environment.getExternalStorageDirectory(), DEFAULT_APP_IMAGEDATA_DIRECTORY);
        if (isExternalStorageWritable() && !mFolder.exists() && !mFolder.mkdirs()) {
            Log.e("TinyDB", "Failed to setup folder");
            return "";
        }
        return mFolder.getPath() + '/' + imageName;
    }

    private boolean saveBitmap(String fullPath, Bitmap bitmap) {
        try {
            File imageFile = new File(fullPath);
            if (imageFile.exists()) imageFile.delete();
            FileOutputStream out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getSavedImagePath() {
        return lastImagePath;
    }

    // ✅ Basic Getters
    public String getString(String key) {
        return preferences.getString(key, "");
    }

    public int getInt(String key) {
        return preferences.getInt(key, 0);
    }

    public boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    public ArrayList<String> getListString(String key) {
        return new ArrayList<>(Arrays.asList(TextUtils.split(preferences.getString(key, ""), "‚‗‚")));
    }

    // ✅ Basic Putters
    public void putString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    public void putInt(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    public void putBoolean(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    public void putListString(String key, ArrayList<String> stringList) {
        String[] myStringList = stringList.toArray(new String[0]);
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myStringList)).apply();
    }

    // ✅ Save and Retrieve Address (still local)
    public void putDeliveryAddress(String key, DeliveryAddress address) {
        preferences.edit().putString(key, new Gson().toJson(address)).apply();
    }

    public DeliveryAddress getDeliveryAddress(String key) {
        String json = getString(key);
        return json.isEmpty() ? null : new Gson().fromJson(json, DeliveryAddress.class);
    }

    // ✅ Other Helpers
    public void remove(String key) {
        preferences.edit().remove(key).apply();
    }

    public void clear() {
        preferences.edit().clear().apply();
    }

    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
}
