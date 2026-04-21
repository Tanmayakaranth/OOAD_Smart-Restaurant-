package com.restaurant.tablemanagement.service;

import com.restaurant.tablemanagement.controller.KitchenWebSocketController;
import com.restaurant.tablemanagement.dto.KitchenUpdateMessage;
import com.restaurant.tablemanagement.model.Order;
import com.restaurant.tablemanagement.model.OrderStatus;
import com.restaurant.billing.service.BillingService;
import com.restaurant.billing.service.DashboardService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * KitchenService handles the business logic for kitchen operations.
 * It manages order processing, scheduling, and status updates.
 * 
 * Responsibilities:
 * - Receive orders from the order service
 * - Apply scheduling strategies
 * - Update order statuses
 * - Track kitchen queue state
 * - Broadcast real-time updates via WebSocket
 */
@Service
public class KitchenService {
    
    private final KitchenQueue kitchenQueue;
    private final OrderService orderService;
    
    // Track completed orders session stats
    private int completedOrderCount = 0;
    private String lastCompletedOrderId = null;
    private String lastCompletedTableId = null;
    
    @Autowired(required = false)
    private KitchenWebSocketController webSocketController;
    
    /**
     * Constructor initializing the KitchenService with its dependencies.
     * 
     * @param orderService The OrderService for order management
     * @param billingService The BillingService for billing operations
     * @param dashboardService The DashboardService for dashboard updates
     */
    public KitchenService(OrderService orderService, BillingService billingService, DashboardService dashboardService) {
        this.orderService = orderService;
        this.kitchenQueue = new KitchenQueue(billingService, dashboardService);
    }
    
    /**
     * Adds an order to the kitchen queue for processing.
     * Broadcasts a real-time update via WebSocket to all connected kitchen dashboards.
     * 
     * @param order The order to be added to the kitchen
     */
    public void addOrderToKitchen(Order order) {
        if (order != null) {
            kitchenQueue.addOrder(order);
            
            // Broadcast update via WebSocket for real-time UI update
            if (webSocketController != null) {
                webSocketController.broadcastUpdate(KitchenUpdateMessage.orderAdded(order));
            }
        }
    }
    
    /**
     * Fetches the next order to be prepared based on the current scheduling strategy.
     * 
     * @return Optional containing the next Order, or empty if no pending orders
     */
    public Optional<Order> getNextOrderToProcess() {
        return kitchenQueue.getNextOrder();
    }
    
    /**
     * Starts preparing an order.
     * Moves the order from pending to preparing state.
     * Broadcasts a real-time update via WebSocket.
     * 
     * @param orderId The ID of the order to start preparing
     * @return true if the order was successfully started, false otherwise
     */
    public boolean startPreparingOrder(String orderId) {
        Optional<Order> order = orderService.getOrderById(orderId);
        boolean result = kitchenQueue.startPreparation(order);
        
        // Broadcast update via WebSocket
        if (result && order.isPresent() && webSocketController != null) {
            webSocketController.broadcastUpdate(KitchenUpdateMessage.orderStarted(order.get()));
        }
        
        return result;
    }
    
    /**
     * Marks an order as ready for delivery.
     * Moves the order from preparing to ready state.
     * Broadcasts a real-time update via WebSocket.
     * 
     * @param orderId The ID of the order that is ready
     * @return true if the order was successfully marked as ready, false otherwise
     */
    public boolean markOrderAsReady(String orderId) {
        Optional<Order> order = orderService.getOrderById(orderId);
        boolean result = kitchenQueue.markAsReady(order);
        
        // Broadcast update via WebSocket
        if (result && order.isPresent() && webSocketController != null) {
            webSocketController.broadcastUpdate(KitchenUpdateMessage.orderReady(order.get()));
        }
        
        return result;
    }
    
    /**
     * Completes an order and removes it from the kitchen queue.
     * Broadcasts a real-time update via WebSocket.
     * Increments session completed order count and tracks last completed table.
     * 
     * @param orderId The ID of the order to complete
     * @return true if the order was successfully completed, false otherwise
     */
    public boolean completeOrder(String orderId) {
        Optional<Order> order = orderService.getOrderById(orderId);
        boolean result = kitchenQueue.completeOrder(order);
        
        // Track completed order stats
        if (result && order.isPresent()) {
            completedOrderCount++;
            Order completedOrder = order.get();
            lastCompletedOrderId = completedOrder.getOrderId();
            lastCompletedTableId = completedOrder.getTableId();
            
            // Broadcast update via WebSocket
            if (webSocketController != null) {
                webSocketController.broadcastUpdate(KitchenUpdateMessage.orderCompleted(completedOrder));
            }
        }
        
        return result;
    }
    
    /**
     * Sets the scheduling strategy for the kitchen queue.
     * 
     * @param strategy The SchedulerStrategy to use (FIFO, Priority, SJF)
     */
    public void setSchedulingStrategy(SchedulerStrategy strategy) {
        kitchenQueue.setSchedulerStrategy(strategy);
    }
    
    /**
     * Sets the scheduling strategy by name.
     * Allows runtime switching between different strategies.
     * 
     * @param strategyName The name of the strategy (FIFO, PRIORITY, SJF)
     */
    public void setSchedulingStrategyByName(String strategyName) {
        SchedulerStrategy strategy;
        
        switch (strategyName.toUpperCase()) {
            case "PRIORITY":
                strategy = new PriorityScheduler();
                break;
            case "SJF":
                strategy = new SJFScheduler();
                break;
            case "FIFO":
            default:
                strategy = new FIFOScheduler();
                break;
        }
        
        setSchedulingStrategy(strategy);
    }
    
    /**
     * Gets the current scheduling strategy being used.
     * 
     * @return The current SchedulerStrategy
     */
    public SchedulerStrategy getCurrentSchedulingStrategy() {
        return kitchenQueue.getSchedulerStrategy();
    }
    
    /**
     * Gets the current scheduling strategy name as a string.
     * 
     * @return The strategy name (FIFO, PRIORITY, SJF)
     */
    public String getCurrentStrategy() {
        return getCurrentSchedulingStrategy().getStrategyName();
    }
    
    /**
     * Gets all pending orders in the kitchen queue.
     * 
     * @return List of pending orders
     */
    public List<Order> getPendingOrders() {
        return kitchenQueue.getPendingOrders();
    }
    
    /**
     * Gets all orders currently being prepared.
     * 
     * @return List of preparing orders
     */
    public List<Order> getPreparingOrders() {
        return kitchenQueue.getPreparingOrders();
    }
    
    /**
     * Gets all orders ready for delivery.
     * 
     * @return List of ready orders
     */
    public List<Order> getReadyOrders() {
        return kitchenQueue.getReadyOrders();
    }
    
    /**
     * Gets all orders currently in the kitchen.
     * 
     * @return List of all orders in the kitchen
     */
    public List<Order> getAllOrdersInKitchen() {
        return kitchenQueue.getAllOrdersInKitchen();
    }
    
    /**
     * Gets statistics about the current kitchen queue state.
     * 
     * @return KitchenQueueStats object containing queue statistics
     */
    public KitchenQueueStats getQueueStatistics() {
        return new KitchenQueueStats(
            kitchenQueue.getPendingOrderCount(),
            kitchenQueue.getPreparingOrderCount(),
            kitchenQueue.getReadyOrderCount(),
            getCurrentSchedulingStrategy().getStrategyName(),
            completedOrderCount,
            lastCompletedTableId,
            lastCompletedOrderId
        );
    }
    
    /**
     * Inner class to represent kitchen queue statistics.
     */
    public static class KitchenQueueStats {
        public int pendingCount;
        public int preparingCount;
        public int readyCount;
        public int completedCount;
        public String currentStrategy;
        public String lastCompletedTable;
        public String lastCompletedOrderId;
        
        public KitchenQueueStats(int pending, int preparing, int ready, String strategy) {
            this(pending, preparing, ready, strategy, 0, null, null);
        }
        
        public KitchenQueueStats(int pending, int preparing, int ready, String strategy, 
                                  int completed, String lastTable, String lastOrderId) {
            this.pendingCount = pending;
            this.preparingCount = preparing;
            this.readyCount = ready;
            this.completedCount = completed;
            this.currentStrategy = strategy;
            this.lastCompletedTable = lastTable;
            this.lastCompletedOrderId = lastOrderId;
        }
    }
}