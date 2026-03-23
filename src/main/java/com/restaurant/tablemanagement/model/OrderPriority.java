//ENUM for order priority levels in restaurant management system
package com.restaurant.tablemanagement.model;

public enum OrderPriority {
    VIP(1),                 // Highest priority
    ONLINE(2),
    NORMAL(3),
    WALK_IN(4);             // Lowest priority
    
    private final int priorityValue;
    
    OrderPriority(int priorityValue) {
        this.priorityValue = priorityValue;
    }
    
    public int getPriorityValue() {
        return priorityValue;
    }
}


/*
Purpose:
Defines order priority levels
Used in OrderPriorityQueue to sort orders
VIP orders (1) processed before WALK_IN (4) */