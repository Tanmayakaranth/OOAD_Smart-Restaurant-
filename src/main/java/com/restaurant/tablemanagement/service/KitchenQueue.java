package com.restaurant.tablemanagement.service;

import com.restaurant.tablemanagement.model.Order;
import com.restaurant.tablemanagement.model.OrderStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * KitchenQueue manages the queue of orders waiting to be prepared.
 * It maintains orders in different states and provides methods to add,
 * remove, and fetch orders based on the selected scheduling strategy.
 */
public class KitchenQueue {
    
    private List<Order> pendingOrders;
    private List<Order> preparingOrders;
    private List<Order> readyOrders;
    private SchedulerStrategy schedulerStrategy;
    
    /**
     * Constructor initializing the kitchen queue with a default FIFO strategy.
     */
    public KitchenQueue() {
        this.pendingOrders = new ArrayList<>();
        this.preparingOrders = new ArrayList<>();
        this.readyOrders = new ArrayList<>();
        this.schedulerStrategy = new FIFOScheduler();
    }
    
    /**
     * Adds an order to the pending queue.
     * The order is inserted in the correct position based on the current scheduling strategy.
     * 
     * @param order The order to be added
     */
    public void addOrder(Order order) {
        if (order != null) {
            order.setStatus(OrderStatus.CREATED);
            pendingOrders.add(order);
            // Re-sort immediately so the queue remains ordered by current strategy
            reorderPendingQueue();
        }
    }
    
    /**
     * Gets the next order to prepare based on the current scheduling strategy.
     * 
     * @return Optional containing the next Order, or empty if no pending orders
     */
    public Optional<Order> getNextOrder() {
        Order nextOrder = schedulerStrategy.selectNextOrder(pendingOrders);
        return Optional.ofNullable(nextOrder);
    }
    
    /**
     * Moves an order from pending to preparing state.
     * 
     * @param order The order to start preparing
     * @return true if the order was successfully moved, false otherwise
     */
    public boolean startPreparation(Optional<Order> order) {
        if (order.isPresent()) {
            Order o = order.get();
            if (o != null && pendingOrders.contains(o)) {
                pendingOrders.remove(o);
                o.setStatus(OrderStatus.PREPARING);
                preparingOrders.add(o);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Moves an order from preparing to ready state.
     * 
     * @param order The order that is ready
     * @return true if the order was successfully marked as ready, false otherwise
     */
    public boolean markAsReady(Optional<Order> order) {
        if (order.isPresent()) {
            Order o = order.get();
            if (o != null && preparingOrders.contains(o)) {
                preparingOrders.remove(o);
                o.setStatus(OrderStatus.READY);
                readyOrders.add(o);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Completes an order and removes it from the kitchen queue.
     * 
     * @param order The order to complete
     * @return true if the order was completed, false otherwise
     */
    public boolean completeOrder(Optional<Order> order) {
        if (order.isPresent()) {
            Order o = order.get();
            if (o != null && readyOrders.contains(o)) {
                readyOrders.remove(o);
                o.setStatus(OrderStatus.SERVED);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Sets the scheduling strategy for order selection and reorders the pending queue accordingly.
     * When the strategy changes, all pending orders are immediately re-sorted according to the new strategy.
     * 
     * @param strategy The SchedulerStrategy to use
     */
    public void setSchedulerStrategy(SchedulerStrategy strategy) {
        if (strategy != null) {
            SchedulerStrategy oldStrategy = this.schedulerStrategy;
            this.schedulerStrategy = strategy;
            
            // Reorder pending orders based on new strategy
            reorderPendingQueue();
            
            String oldStrategyName = oldStrategy != null ? oldStrategy.getStrategyName() : "UNKNOWN";
            System.out.println("🔄 Scheduling strategy changed: " + oldStrategyName + " → " + strategy.getStrategyName() + 
                             " | Reordered " + pendingOrders.size() + " pending orders");
        }
    }
    
    /**
     * Reorders the pending queue based on the current scheduler strategy.
     * This ensures that orders are always processed according to the active strategy.
     * Called whenever:
     * - A new order is added (to insert in correct position)
     * - The strategy is changed (to reorganize existing orders)
     */
    private void reorderPendingQueue() {
        if (pendingOrders == null || pendingOrders.isEmpty()) {
            return;
        }
        
        if (schedulerStrategy == null) {
            // Default to FIFO if no strategy set
            pendingOrders.sort((o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()));
            return;
        }
        
        // Sort based on the current strategy's logic
        if (schedulerStrategy instanceof PriorityScheduler) {
            // Sort by priority value (lower = higher priority), then FIFO
            pendingOrders.sort((o1, o2) -> {
                int priority1 = o1.getOrderPriority().getPriorityValue();
                int priority2 = o2.getOrderPriority().getPriorityValue();
                
                if (priority1 != priority2) {
                    return Integer.compare(priority1, priority2);
                }
                // Same priority: process older orders first (FIFO)
                return o1.getCreatedAt().compareTo(o2.getCreatedAt());
            });
        } else if (schedulerStrategy instanceof SJFScheduler) {
            // Sort by item count (fewer items first), then FIFO
            pendingOrders.sort((o1, o2) -> {
                int itemCountComparison = Integer.compare(
                    o1.getOrderItems().size(),
                    o2.getOrderItems().size()
                );
                if (itemCountComparison == 0) {
                    // Same item count: process older orders first (FIFO)
                    return o1.getCreatedAt().compareTo(o2.getCreatedAt());
                }
                return itemCountComparison;
            });
        } else if (schedulerStrategy instanceof FIFOScheduler) {
            // FIFO: simply sort by creation time
            pendingOrders.sort((o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()));
        } else {
            // Unknown strategy: default to FIFO
            pendingOrders.sort((o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()));
        }
    }
    
    /**
     * Gets the current scheduling strategy.
     * 
     * @return The current SchedulerStrategy
     */
    public SchedulerStrategy getSchedulerStrategy() {
        return schedulerStrategy;
    }
    
    /**
     * Gets all pending orders.
     * 
     * @return List of pending orders
     */
    public List<Order> getPendingOrders() {
        return new ArrayList<>(pendingOrders);
    }
    
    /**
     * Gets all orders currently being prepared.
     * 
     * @return List of preparing orders
     */
    public List<Order> getPreparingOrders() {
        return new ArrayList<>(preparingOrders);
    }
    
    /**
     * Gets all orders ready for delivery.
     * 
     * @return List of ready orders
     */
    public List<Order> getReadyOrders() {
        return new ArrayList<>(readyOrders);
    }
    
    /**
     * Gets all orders in the kitchen (pending + preparing + ready).
     * 
     * @return List of all orders in the kitchen
     */
    public List<Order> getAllOrdersInKitchen() {
        List<Order> allOrders = new ArrayList<>();
        allOrders.addAll(pendingOrders);
        allOrders.addAll(preparingOrders);
        allOrders.addAll(readyOrders);
        return allOrders;
    }
    
    /**
     * Gets the count of pending orders.
     * 
     * @return Number of pending orders
     */
    public int getPendingOrderCount() {
        return pendingOrders.size();
    }
    
    /**
     * Gets the count of orders being prepared.
     * 
     * @return Number of preparing orders
     */
    public int getPreparingOrderCount() {
        return preparingOrders.size();
    }
    
    /**
     * Gets the count of ready orders.
     * 
     * @return Number of ready orders
     */
    public int getReadyOrderCount() {
        return readyOrders.size();
    }
}