package com.restaurant.billing.model;

import java.time.LocalDateTime;

public class Bill {

    private String billId;
    private String orderId;

    private double totalAmount;
    private double tax;
    private double discount;
    private double finalAmount;

    private LocalDateTime timestamp;

    public Bill(String orderId, double totalAmount, double tax, double discount, double finalAmount) {
        this.billId = "BILL-" + System.currentTimeMillis();
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.tax = tax;
        this.discount = discount;
        this.finalAmount = finalAmount;
        this.timestamp = LocalDateTime.now();
    }

    // getters
    public String getBillId() { return billId; }
    public String getOrderId() { return orderId; }
    public double getTotalAmount() { return totalAmount; }
    public double getTax() { return tax; }
    public double getDiscount() { return discount; }
    public double getFinalAmount() { return finalAmount; }
    public LocalDateTime getTimestamp() { return timestamp; }
}