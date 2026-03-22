package com.restaurant.tablemanagement.dto;

public class CustomerRequest {

    private int customerId;
    private int groupSize;
    private boolean isVIP;

    public int getCustomerId() {
        return customerId;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public boolean isVIP() {
        return isVIP;
    }
}
