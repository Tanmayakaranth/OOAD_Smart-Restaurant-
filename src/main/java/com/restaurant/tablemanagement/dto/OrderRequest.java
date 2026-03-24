//DTO - for API Requests
package com.restaurant.tablemanagement.dto;

import com.restaurant.tablemanagement.model.OrderPriority;
import java.util.*;

public class OrderRequest {
    private String customerId;
    private String tableId;
    private OrderPriority orderPriority;
    private boolean isVIP;              // Track if customer is VIP
    private List<OrderItemRequest> items;
    
    // Inner class for order items
    public static class OrderItemRequest {
        private String menuItemId;
        private Integer quantity;
        private String specialNotes;
        
        // Getters & Setters
        public String getMenuItemId() { return menuItemId; }
        public void setMenuItemId(String menuItemId) { this.menuItemId = menuItemId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public String getSpecialNotes() { return specialNotes; }
        public void setSpecialNotes(String notes) { this.specialNotes = notes; }
    }
    
    // Getters & Setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }
    
    public OrderPriority getOrderPriority() { return orderPriority; }
    public void setOrderPriority(OrderPriority priority) { this.orderPriority = priority; }
    
    public boolean isVIP() { return isVIP; }
    public void setVIP(boolean vip) { this.isVIP = vip; }
    
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}


/*Purpose:

API request format
Client sends this when creating an order
Cleaner than passing raw Order object

Example Usage:
json{
    "customerId": "CUST-123",
    "tableId": "TABLE-5",
    "isVIP": true,
    "orderPriority": "NORMAL",  // Will be overridden to VIP if isVIP is true
    "items": [
        {"menuItemId": "MENU-001", "quantity": 2, "specialNotes": "No onions"},
        {"menuItemId": "MENU-005", "quantity": 1, "specialNotes": ""}
    ]
} */