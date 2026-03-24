package com.restaurant.tablemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.restaurant.tablemanagement.model.Order;
import java.util.List;

/**
 * DTO for sending kitchen updates via WebSocket.
 * Contains all information needed to update the kitchen dashboard in real-time.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KitchenUpdateMessage {
    
    // Update type constants
    public static final String UPDATE_TYPE_ORDER_ADDED = "ORDER_ADDED";
    public static final String UPDATE_TYPE_ORDER_STARTED = "ORDER_STARTED";
    public static final String UPDATE_TYPE_ORDER_READY = "ORDER_READY";
    public static final String UPDATE_TYPE_ORDER_COMPLETED = "ORDER_COMPLETED";
    public static final String UPDATE_TYPE_STRATEGY_CHANGED = "STRATEGY_CHANGED";
    public static final String UPDATE_TYPE_FULL_REFRESH = "FULL_REFRESH";
    
    /**
     * Type of update (ORDER_ADDED, ORDER_STARTED, ORDER_READY, ORDER_COMPLETED, STRATEGY_CHANGED, FULL_REFRESH)
     */
    private String updateType;
    
    /**
     * The affected order (when applicable)
     */
    private Order order;
    
    /**
     * List of pending orders for full refresh
     */
    private List<Order> pendingOrders;
    
    /**
     * List of preparing orders for full refresh
     */
    private List<Order> preparingOrders;
    
    /**
     * List of ready orders for full refresh
     */
    private List<Order> readyOrders;
    
    /**
     * Current scheduling strategy
     */
    private String currentStrategy;
    
    /**
     * Statistics snapshot
     */
    private Object statistics;
    
    /**
     * Timestamp of the update
     */
    private long timestamp;
    
    /**
     * Error message if update failed (optional)
     */
    private String errorMessage;
    
    /**
     * Helper method to create an order added message
     */
    public static KitchenUpdateMessage orderAdded(Order order) {
        KitchenUpdateMessage msg = new KitchenUpdateMessage();
        msg.setUpdateType(UPDATE_TYPE_ORDER_ADDED);
        msg.setOrder(order);
        msg.setTimestamp(System.currentTimeMillis());
        return msg;
    }
    
    /**
     * Helper method to create an order started message
     */
    public static KitchenUpdateMessage orderStarted(Order order) {
        KitchenUpdateMessage msg = new KitchenUpdateMessage();
        msg.setUpdateType(UPDATE_TYPE_ORDER_STARTED);
        msg.setOrder(order);
        msg.setTimestamp(System.currentTimeMillis());
        return msg;
    }
    
    /**
     * Helper method to create an order ready message
     */
    public static KitchenUpdateMessage orderReady(Order order) {
        KitchenUpdateMessage msg = new KitchenUpdateMessage();
        msg.setUpdateType(UPDATE_TYPE_ORDER_READY);
        msg.setOrder(order);
        msg.setTimestamp(System.currentTimeMillis());
        return msg;
    }
    
    /**
     * Helper method to create an order completed message
     */
    public static KitchenUpdateMessage orderCompleted(Order order) {
        KitchenUpdateMessage msg = new KitchenUpdateMessage();
        msg.setUpdateType(UPDATE_TYPE_ORDER_COMPLETED);
        msg.setOrder(order);
        msg.setTimestamp(System.currentTimeMillis());
        return msg;
    }
}
