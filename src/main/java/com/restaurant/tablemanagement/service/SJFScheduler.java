package com.restaurant.tablemanagement.service;

import com.restaurant.tablemanagement.model.Order;
import java.util.List;

/**
 * SJF (Shortest Job First) Scheduling Strategy.
 * Orders with fewer items (simpler orders) are processed first.
 * Minimizes overall average waiting time and maximizes throughput.
 * 
 * The pending queue is kept sorted by item count, so this scheduler
 * simply returns the first order in the queue.
 */
public class SJFScheduler implements SchedulerStrategy {
    
    @Override
    public Order selectNextOrder(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return null;
        }
        
        // Queue is maintained in item count order by KitchenQueue.reorderPendingQueue()
        // Orders with fewer items come first
        // So return the first order which is the shortest job
        return orders.get(0);
    }
    
    @Override
    public String getStrategyName() {
        return "SJF (Shortest Job First)";
    }
}