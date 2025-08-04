package com.example.onlineshop.Model;

import java.util.ArrayList;
import java.util.HashMap;

public class OrderModel {
    private String orderId;
    private String paymentId;
    private String userId;
    private String address;
    private String phone;
    private double subtotal;
    private double tax;
    private double delivery;
    private double totalAmount;
    private long timestamp;
    private String status;
    private ArrayList<HashMap<String, Object>> items;

    public OrderModel() {
        // Default constructor required for calls to DataSnapshot.getValue(OrderModel.class)
    }

    public OrderModel(String orderId, String paymentId, String userId, String address, String phone,
                      double subtotal, double tax, double delivery, double totalAmount, long timestamp,
                      String status, ArrayList<HashMap<String, Object>> items) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.userId = userId;
        this.address = address;
        this.phone = phone;
        this.subtotal = subtotal;
        this.tax = tax;
        this.delivery = delivery;
        this.totalAmount = totalAmount;
        this.timestamp = timestamp;
        this.status = status;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public double getDelivery() {
        return delivery;
    }

    public void setDelivery(double delivery) {
        this.delivery = delivery;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<HashMap<String, Object>> getItems() {
        return items;
    }

    public void setItems(ArrayList<HashMap<String, Object>> items) {
        this.items = items;
    }
}
