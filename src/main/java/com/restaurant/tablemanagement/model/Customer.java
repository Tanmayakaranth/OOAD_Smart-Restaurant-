package com.restaurant.tablemanagement.model;

public class Customer {

    private int customerId;
    private int groupSize;
    private boolean isVIP;
    private long arrivalTime;  // 🔥 NEW

    public Customer(int customerId, int groupSize, boolean isVIP) {
        this.customerId = customerId;
        this.groupSize = groupSize;
        this.isVIP = isVIP;
        this.arrivalTime = System.currentTimeMillis(); // 🔥 IMPORTANT
    }

    public int getCustomerId() { return customerId; }
    public int getGroupSize() { return groupSize; }
    public boolean isVIP() { return isVIP; }
    public long getArrivalTime() { return arrivalTime; } // 🔥 NEW
}