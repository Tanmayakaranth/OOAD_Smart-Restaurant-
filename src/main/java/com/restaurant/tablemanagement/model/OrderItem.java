//CLASS representing an individual item in a customer's order
package com.restaurant.tablemanagement.model;

public class OrderItem {
    private String itemId;              // Unique ID for this order item
    private String menuItemId;          // Which menu item (food/beverage)
    private Integer quantity;
    private String specialNotes;        // e.g., "No onions", "Extra spice"
    private OrderStatus itemStatus;     // ADDED, PREPARING, READY, REMOVED
    
    // Constructor
    public OrderItem(String menuItemId, Integer quantity) {
        this.menuItemId = menuItemId;
        this.quantity = quantity;
        this.itemStatus = OrderStatus.CREATED;
        this.itemId = generateItemId();
    }
    
    // Methods
    public void updateQuantity(Integer newQuantity) {
        this.quantity = newQuantity;
    }
    
    public void removeItem() {
        this.itemStatus = OrderStatus.CANCELLED;
    }
    
    public void setItemStatus(OrderStatus status) {
        this.itemStatus = status;
    }
    
    private String generateItemId() {
        return "ITEM-" + System.currentTimeMillis();
    }
    
    // Getters & Setters
    public String getItemId() { return itemId; }
    public String getMenuItemId() { return menuItemId; }
    public Integer getQuantity() { return quantity; }
    public String getSpecialNotes() { return specialNotes; }
    public void setSpecialNotes(String notes) { this.specialNotes = notes; }
    public OrderStatus getItemStatus() { return itemStatus; }
}

/*Purpose:
Represents individual items in an order
Can be modified or removed before cooking
Tracks status separately from main order

Integration with Person 1:
Each OrderItem linked to menu (future integration) */