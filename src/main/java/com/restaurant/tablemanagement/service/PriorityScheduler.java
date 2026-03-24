package com.restaurant.tablemanagement.service;

import com.restaurant.tablemanagement.model.Order;
import com.restaurant.tablemanagement.model.OrderPriority;
import java.util.List;

/**
 * Priority-Based Scheduling Strategy.
 * Orders with higher priority (VIP, urgent, etc.) are processed first.
 * Within the same priority level, FIFO is applied.
 * 
 * The pending queue is kept sorted by priority value, so this scheduler
 * simply returns the first order in the queue.
 */
public class PriorityScheduler implements SchedulerStrategy {
    
    @Override
    public Order selectNextOrder(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return null;
        }
        
        // Queue is maintained in priority order by KitchenQueue.reorderPendingQueue()
        // VIP (1) > ONLINE (2) > NORMAL (3) > WALK_IN (4)
        // So return the first order which is the highest priority
        return orders.get(0);
    }
    
    @Override
    public String getStrategyName() {
        return "Priority-Based Scheduling";
    }
}