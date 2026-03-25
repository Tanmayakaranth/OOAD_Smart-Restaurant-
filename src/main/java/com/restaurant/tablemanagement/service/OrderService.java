package com.restaurant.tablemanagement.service;

import com.restaurant.tablemanagement.model.*;
import com.restaurant.tablemanagement.dto.OrderRequest;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private RestaurantManager restaurantManager;
    
    // In-memory storage (replace with database later)
    private Map<String, Order> orders = new HashMap<>();
    private OrderPriorityQueue orderQueue;
    
    public OrderService() {
        this.orderQueue = new OrderPriorityQueue();
    }
    
    // 🔥 SETTER for testing/dependency injection
    public void setRestaurantManager(RestaurantManager manager) {
        this.restaurantManager = manager;
    }
    
    // ===== CREATE ORDER (Factory Pattern) =====
    public Order createOrder(OrderRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Order must contain at least one item");
        }

        if (request.getTableId() == null || request.getTableId().isBlank()) {
            throw new RuntimeException("Table ID is required");
        }

        int tableNumber = extractTableNumber(request.getTableId());
        Table table = restaurantManager.getTableById(tableNumber);
        if (table == null) {
            throw new RuntimeException("Table not found: " + request.getTableId());
        }

        if (table.getStatus() == TableStatus.CLEANING) {
            throw new RuntimeException("Table is currently cleaning: " + request.getTableId());
        }

        restaurantManager.occupyTable(tableNumber);

        // 🔥 NEW: Auto-set VIP priority if customer is seated at a VIP table
        OrderPriority priority = request.getOrderPriority();
        if (priority == null) {
            priority = table.isVIP() ? OrderPriority.VIP : OrderPriority.NORMAL;
        }

        // Create new order
        Order order = new Order(
            request.getCustomerId(),
            toCanonicalTableId(tableNumber),
            priority
        );
        
        // Add items from request
        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            OrderItem item = new OrderItem(
                itemReq.getMenuItemId(),
                itemReq.getQuantity()
            );
            item.setSpecialNotes(itemReq.getSpecialNotes());
            order.addItem(item);
        }
        
        // Store order
        orders.put(order.getOrderId(), order);
        
        // Add to priority queue
        orderQueue.enqueueOrder(order);
        
        return order;
    }
    
    // ===== MODIFY ORDER (Before Cooking) =====
    public Order addItemToOrder(String orderId, OrderRequest.OrderItemRequest itemReq) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        
        if (order.getOrderStatus() == OrderStatus.PREPARING) {
            throw new RuntimeException("Cannot add items while order is being prepared");
        }
        
        OrderItem item = new OrderItem(
            itemReq.getMenuItemId(),
            itemReq.getQuantity()
        );
        item.setSpecialNotes(itemReq.getSpecialNotes());
        order.addItem(item);
        
        return order;
    }
    
    public Order removeItemFromOrder(String orderId, String itemId) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        
        if (order.getOrderStatus() == OrderStatus.PREPARING) {
            throw new RuntimeException("Cannot remove items while order is being prepared");
        }
        
        order.removeItem(itemId);
        return order;
    }
    
    public Order modifyItemQuantity(String orderId, String itemId, Integer newQuantity) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        
        if (order.getOrderStatus() != OrderStatus.CREATED) {
            throw new RuntimeException("Can only modify quantity for CREATED orders");
        }
        
        order.modifyItem(itemId, newQuantity);
        return order;
    }
    
    // ===== CANCEL ORDER =====
    public Order cancelOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        
        order.cancelOrder();
        orderQueue.removeOrder(orderId);  // Remove from queue if exists
        tryReleaseTableIfNoActiveOrders(order.getTableId());
        
        return order;
    }
    
    // ===== STATUS UPDATES =====
    public Order updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        
        order.updateOrderStatus(newStatus);

        if (newStatus == OrderStatus.SERVED || newStatus == OrderStatus.CANCELLED) {
            orderQueue.removeOrder(orderId);
            tryReleaseTableIfNoActiveOrders(order.getTableId());
        }

        return order;
    }
    
    // ===== RETRIEVE ORDERS =====
    public Order getOrderById(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        return order;
    }
    
    public List<Order> getOrdersByTable(String tableId) {
        int normalizedTableNo = extractTableNumber(tableId);
        String canonicalTableId = toCanonicalTableId(normalizedTableNo);

        return orders.values().stream()
            .filter(o -> o.getTableId().equals(canonicalTableId))
            .collect(Collectors.toList());
    }
    
    public List<Order> getOrdersByCustomer(String customerId) {
        return orders.values().stream()
            .filter(o -> o.getCustomerId().equals(customerId))
            .collect(Collectors.toList());
    }
    
    public List<Order> getActiveOrders() {
        return orders.values().stream()
            .filter(o -> o.getOrderStatus() != OrderStatus.SERVED && 
                        o.getOrderStatus() != OrderStatus.CANCELLED)
            .collect(Collectors.toList());
    }
    
    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }
    
    // ===== PRIORITY QUEUE ACCESS =====
    public OrderPriorityQueue getOrderQueue() {
        return orderQueue;
    }
    
    public Order getNextOrderFromQueue() {
        return orderQueue.dequeueOrder();
    }
    
    public void enableRushHourMode(boolean enabled) {
        orderQueue.enableRushHourMode(enabled);
    }

    public int cancelActiveOrdersForTable(String tableId) {
        int normalizedTableNo = extractTableNumber(tableId);
        String canonicalTableId = toCanonicalTableId(normalizedTableNo);

        List<Order> activeOrders = orders.values().stream()
                .filter(this::isActiveOrder)
                .filter(order -> order.getTableId().equals(canonicalTableId))
                .collect(Collectors.toList());

        activeOrders.forEach(order -> {
            order.cancelOrder();
            orderQueue.removeOrder(order.getOrderId());
        });

        return activeOrders.size();
    }

    private boolean isActiveOrder(Order order) {
        return order.getOrderStatus() != OrderStatus.SERVED && order.getOrderStatus() != OrderStatus.CANCELLED;
    }

    private void tryReleaseTableIfNoActiveOrders(String tableId) {
        int normalizedTableNo = extractTableNumber(tableId);
        String canonicalTableId = toCanonicalTableId(normalizedTableNo);

        boolean hasActiveOrders = orders.values().stream()
                .anyMatch(order -> order.getTableId().equals(canonicalTableId) && isActiveOrder(order));

        if (!hasActiveOrders) {
            Table table = restaurantManager.getTableById(normalizedTableNo);
            if (table != null && table.getStatus() == TableStatus.OCCUPIED) {
                restaurantManager.freeTable(normalizedTableNo);
            }
        }
    }

    private int extractTableNumber(String tableIdRaw) {
        if (tableIdRaw == null || tableIdRaw.isBlank()) {
            throw new RuntimeException("Invalid table ID");
        }

        String normalized = tableIdRaw.trim().toUpperCase();
        String digits = normalized.replaceAll("[^0-9]", "");

        if (digits.isEmpty()) {
            throw new RuntimeException("Table ID must contain a number: " + tableIdRaw);
        }

        return Integer.parseInt(digits);
    }

    private String toCanonicalTableId(int tableNumber) {
        return "TABLE-" + tableNumber;
    }
}


/*Purpose:
Handles all order operations
Creates orders from OrderRequest
Modifies/cancels orders
Retrieves orders by various filters
Manages priority queue

Key Integration:
Works with OrderPriorityQueue (Person 2)
Will work with Person 3's Kitchen system (dequeue orders)
Will work with Person 4's Billing system (retrieve order amounts)*/