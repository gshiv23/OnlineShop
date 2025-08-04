package com.example.onlineshop.Domain;

public class ProductDomain {
    private String id;
    private String title;
    private String pic; // Base64-encoded image
    private String review;
    private double score;
    private double price;
    private String description;
    private String category;

    // Required empty constructor for Firebase
    public ProductDomain() {
    }

    // Full constructor
    public ProductDomain(String id, String title, String pic, String review,
                         double score, double price, String description, String category) {
        this.id = id;
        this.title = title;
        this.pic = pic;
        this.review = review;
        this.score = score;
        this.price = price;
        this.description = description;
        this.category = category;
    }

    // Getters and Setters
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

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // For clarity when Adapter refers to image
    public String getImageBase64() {
        return pic;
    }
}
