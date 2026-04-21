//ENUM for order status in restaurant management system
package com.restaurant.tablemanagement.model;

public enum OrderStatus {
    CREATED,        // Just created, not sent to kitchen
    PREPARING,      // Being prepared in kitchen
    READY,          // Ready to serve
    SERVED,         // Delivered to customer
    CANCELLED       // Order cancelled
}

/*Purpose:
Tracks order lifecycle
Updates as order moves through system
Used for filtering active/completed orders */