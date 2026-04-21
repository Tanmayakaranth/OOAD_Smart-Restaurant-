//Priority Logic
package com.restaurant.tablemanagement.service;

import com.restaurant.tablemanagement.model.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class OrderPriorityQueue {
    
    // Priority queue that sorts by priority value
    private PriorityQueue<Order> queue;
    private boolean rushHourMode = false;
    
    public OrderPriorityQueue() {
        // Comparator: lower priority value = higher importance
        this.queue = new PriorityQueue<>((o1, o2) -> {
            int priority1 = o1.getOrderPriority().getPriorityValue();
            int priority2 = o2.getOrderPriority().getPriorityValue();
            
            if (priority1 != priority2) {
                return Integer.compare(priority1, priority2);  // By priority
            }
            
            // If same priority, older order first (FIFO)
            return o1.getCreatedAt().compareTo(o2.getCreatedAt());
        });
    }
    
    // ===== QUEUE OPERATIONS =====
    public void enqueueOrder(Order order) {
        if (order.getOrderStatus() == OrderStatus.CREATED) {
            queue.add(order);
        }
    }
    
    public Order dequeueOrder() {
        if (queue.isEmpty()) {
            return null;
        }
        Order nextOrder = queue.poll();
        // Update status to PREPARING when kitchen actually picks the order
        nextOrder.updateOrderStatus(OrderStatus.PREPARING);
        return nextOrder;  // Removes and returns highest priority order
    }
    
    public Order peekNextOrder() {
        return queue.peek();  // View without removing
    }
    
    public int getQueueSize() {
        return queue.size();
    }
    
    public boolean isEmpty() {
        return queue.isEmpty();
    }
    
    // ===== VIEW QUEUE =====
    public List<Order> viewQueue() {
        return new ArrayList<>(queue);
    }
    
    public void removeOrder(String orderId) {
        queue.removeIf(o -> o.getOrderId().equals(orderId));
    }
    
    // ===== RUSH HOUR MODE =====
    public void enableRushHourMode(boolean enabled) {
        this.rushHourMode = enabled;
        // When rush hour enabled, might adjust priorities dynamically
        // Example: increase NORMAL priority temporarily
    }
    
    public boolean isRushHourMode() {
        return rushHourMode;
    }
    
    // ===== STATISTICS =====
    public int getOrderCountByPriority(OrderPriority priority) {
        return (int) queue.stream()
            .filter(o -> o.getOrderPriority() == priority)
            .count();
    }
    
    public double getAverageWaitTime() {
        if (queue.isEmpty()) return 0;
        return queue.stream()
            .mapToLong(o -> System.currentTimeMillis() - o.getCreatedAt().getNano())
            .average()
            .orElse(0);
    }
}

/*Purpose:
Maintains order of orders to be cooked
Sorts by priority (VIP first)
Used by Person 3's Kitchen system

How it Works:
OrderService calls enqueueOrder() when order is created
Kitchen system calls dequeueOrder() to get next order
PriorityQueue automatically sorts: VIP(1) > ONLINE(2) > NORMAL(3) > WALK_IN(4) */