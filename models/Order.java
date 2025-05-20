// Order.java
package com.example.legacy_order.models;

public class Order {
    private String orderId;
    private String productName;
    private String buyerId;  // Changed from buyerName
    private double price;
    private String sellerId;
    private String pickupDate;
    private String pickupTime;
    private String pickupLocation;
    private String status;

    // Required empty constructor
    public Order() {}

    // Constructor with all fields
    public Order(String orderId, String productName, String buyerId, double price,
                 String sellerId, String pickupDate, String pickupTime,
                 String pickupLocation, String status) {
        this.orderId = orderId;
        this.productName = productName;
        this.buyerId = buyerId;
        this.price = price;
        this.sellerId = sellerId;
        this.pickupDate = pickupDate;
        this.pickupTime = pickupTime;
        this.pickupLocation = pickupLocation;
        this.status = status;
    }

    // Getters and setters for all fields
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getPickupDate() { return pickupDate; }
    public void setPickupDate(String pickupDate) { this.pickupDate = pickupDate; }
    public String getPickupTime() { return pickupTime; }
    public void setPickupTime(String pickupTime) { this.pickupTime = pickupTime; }
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}