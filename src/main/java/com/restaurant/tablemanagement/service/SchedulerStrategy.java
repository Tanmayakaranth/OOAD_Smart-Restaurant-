package com.restaurant.tablemanagement.service;

import com.restaurant.tablemanagement.model.Order;
import java.util.List;

/**
 * Strategy interface for different order scheduling algorithms.
 * Implements the Strategy Design Pattern to allow switching between
 * different scheduling strategies at runtime.
 */
public interface SchedulerStrategy {
    
    /**
     * Selects the next order to be processed based on the scheduling algorithm.
     * 
     * @param orders List of orders pending in the kitchen queue
     * @return The next Order to be processed, or null if no orders available
     */
    Order selectNextOrder(List<Order> orders);
    
    /**
     * Returns the name of the scheduling strategy.
     * 
     * @return String representation of the strategy name
     */
    String getStrategyName();
}