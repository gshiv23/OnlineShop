package com.example.onlineshop.Domain;

import java.io.Serializable;
import java.util.UUID;

public class PopularDomain implements Serializable {
    private String id;            // ✅ Unique ID
    private String title;
    private String pic;           // ✅ Changed from picUrl to pic
    private int review;
    private double score;
    private int numberInCart;
    private double price;
    private String description;
    private boolean inWishlist;

    public PopularDomain() {
        // ✅ Empty constructor required for Firebase
    }

    public PopularDomain(String title, String pic, int review, double score, double price, String description) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.pic = pic;
        this.review = review;
        this.score = score;
        this.price = price;
        this.description = description;
        this.numberInCart = 0;
        this.inWishlist = false;
    }

    public PopularDomain(String id, String title, String pic, int review, double score, double price, String description) {
        this.id = (id != null ? id : UUID.randomUUID().toString());
        this.title = title;
        this.pic = pic;
        this.review = review;
        this.score = score;
        this.price = price;
        this.description = description;
        this.numberInCart = 0;
        this.inWishlist = false;
    }

    // ✅ Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public int getReview() {
        return review;
    }

    public void setReview(int review) {
        this.review = review;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getNumberInCart() {
        return numberInCart;
    }

    public void setNumberInCart(int numberInCart) {
        this.numberInCart = numberInCart;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isInWishlist() {
        return inWishlist;
    }

    public void setInWishlist(boolean inWishlist) {
        this.inWishlist = inWishlist;
    }
}
