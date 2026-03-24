package com.restaurant.tablemanagement.service;

import com.restaurant.tablemanagement.model.Order;
import java.util.List;

/**
 * FIFO (First-In-First-Out) Scheduling Strategy.
 * Orders are processed in the order they were received.
 * Simple but may not account for order complexity or priority.
 */
public class FIFOScheduler implements SchedulerStrategy {
    
    @Override
    public Order selectNextOrder(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return null;
        }
        // Return the first order in the queue
        return orders.get(0);
    }
    
    @Override
    public String getStrategyName() {
        return "FIFO (First-In-First-Out)";
    }
}