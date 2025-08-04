package com.example.onlineshop.Domain;

public class ProductDomain {
    private String id;
    private String title;
    private String review;
    private double score;
    private double price;
    private String description;
    private String category;
    private String pic;

    public ProductDomain() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPic() { return pic; }
    public void setPic(String pic) { this.pic = pic; }
}
