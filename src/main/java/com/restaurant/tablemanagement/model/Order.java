//MAIN MODEL
//CLASS for customer orders in restaurant management system
package com.restaurant.tablemanagement.model;

import java.time.LocalDateTime;
import java.util.*;

public class Order {
    private String orderId;
    private String customerId;          // Links to Customer (Person 1)
    private String tableId;             // Links to Table (Person 1)
    private List<OrderItem> orderItems;
    private OrderPriority orderPriority;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
    private LocalDateTime completionTime;
    private Double totalAmount;
    private boolean isCustomerVIP;      // Track if customer is VIP for kitchen processing
    
    // Constructor
    public Order(String customerId, String tableId, OrderPriority priority) {
        this.orderId = generateOrderId();
        this.customerId = customerId;
        this.tableId = tableId;
        this.orderPriority = priority;
        this.orderStatus = OrderStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.orderItems = new ArrayList<>();
        this.totalAmount = 0.0;
        this.isCustomerVIP = false;
    }
    
    // Constructor with VIP flag
    public Order(String customerId, String tableId, OrderPriority priority, boolean isVIP) {
        this.orderId = generateOrderId();
        this.customerId = customerId;
        this.tableId = tableId;
        this.orderPriority = priority;
        this.orderStatus = OrderStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.orderItems = new ArrayList<>();
        this.totalAmount = 0.0;
        this.isCustomerVIP = isVIP;
    }
    
    // Methods
    public void addItem(OrderItem item) {
        if (orderStatus == OrderStatus.CREATED || orderStatus == OrderStatus.PREPARING) {
            orderItems.add(item);
            updateTotal();
        }
    }
    
    public void removeItem(String itemId) {
        boolean removed = orderItems.removeIf(i -> i.getItemId().equals(itemId));
        if (!removed) {
            throw new IllegalArgumentException("Item not found in order: " + itemId);
        }
        updateTotal();
    }
    
    public void modifyItem(String itemId, Integer newQuantity) {
        OrderItem item = orderItems.stream()
            .filter(i -> i.getItemId().equals(itemId))
            .findFirst()
            .orElse(null);
        
        if (item == null) {
            throw new IllegalArgumentException("Item not found in order: " + itemId);
        }

        if (orderStatus == OrderStatus.CREATED) {
            item.updateQuantity(newQuantity);
            updateTotal();
            return;
        }

        throw new IllegalStateException("Can only modify items while order is in CREATED state");
    }
    
    public void cancelOrder() {
        if (orderStatus == OrderStatus.CREATED || orderStatus == OrderStatus.PREPARING) {
            orderStatus = OrderStatus.CANCELLED;
            for (OrderItem item : orderItems) {
                item.setItemStatus(OrderStatus.CANCELLED);
            }
        }
    }
    
    public void updateOrderStatus(OrderStatus status) {
        this.orderStatus = status;
        if (status == OrderStatus.SERVED) {
            this.completionTime = LocalDateTime.now();
        }
    }
    
    private void updateTotal() {
        // Placeholder: integrate with MenuItem prices later
        totalAmount = orderItems.stream()
            .mapToDouble(i -> i.getQuantity() * 100.0) // Mock price: 100 per quantity
            .sum();
    }
    
    private String generateOrderId() {
        return "ORD-" + System.currentTimeMillis();
    }
    
    // Getters & Setters
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getTableId() { return tableId; }
    public List<OrderItem> getOrderItems() { return orderItems; }
    public OrderPriority getOrderPriority() { return orderPriority; }
    public OrderPriority getPriority() { return orderPriority; }
    public OrderStatus getOrderStatus() { return orderStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletionTime() { return completionTime; }
    public Double getTotalAmount() { return totalAmount; }
    public boolean isCustomerVIP() { return isCustomerVIP; }
    public void setCustomerVIP(boolean vip) { this.isCustomerVIP = vip; }
    
    public void setStatus(OrderStatus status) { 
        updateOrderStatus(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }
}

/*Purpose:
Core order entity
Contains multiple OrderItems
Linked to TABLE and CUSTOMER (Person 1's models)
Tracks entire lifecycle
Tracks customer VIP status for kitchen prioritization

Integration Points:
tableId → Person 1's Table system
customerId → Person 1's Customer system
orderPriority → Used by OrderPriorityQueue
isCustomerVIP → Automatically upgrades priority for VIP customers */