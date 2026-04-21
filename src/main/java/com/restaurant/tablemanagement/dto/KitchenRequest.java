package com.restaurant.tablemanagement.dto;

/**
 * KitchenRequest DTO for handling kitchen-related HTTP requests.
 * Used for sending scheduling strategy changes and other kitchen operations.
 */
public class KitchenRequest {
    
    private String strategy;
    private Long orderId;
    private String action;
    
    /**
     * Default constructor.
     */
    public KitchenRequest() {
    }
    
    /**
     * Constructor with strategy parameter.
     * 
     * @param strategy The scheduling strategy name (FIFO, PRIORITY, SJF)
     */
    public KitchenRequest(String strategy) {
        this.strategy = strategy;
    }
    
    /**
     * Constructor with orderId and action.
     * 
     * @param orderId The ID of the order
     * @param action The action to perform (start, ready, complete)
     */
    public KitchenRequest(Long orderId, String action) {
        this.orderId = orderId;
        this.action = action;
    }
    
    /**
     * Gets the scheduling strategy name.
     * 
     * @return The strategy name
     */
    public String getStrategy() {
        return strategy;
    }
    
    /**
     * Sets the scheduling strategy name.
     * 
     * @param strategy The strategy name to set
     */
    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
    
    /**
     * Gets the order ID.
     * 
     * @return The order ID
     */
    public Long getOrderId() {
        return orderId;
    }
    
    /**
     * Sets the order ID.
     * 
     * @param orderId The order ID to set
     */
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    /**
     * Gets the action to perform.
     * 
     * @return The action name
     */
    public String getAction() {
        return action;
    }
    
    /**
     * Sets the action to perform.
     * 
     * @param action The action to set
     */
    public void setAction(String action) {
        this.action = action;
    }
    
    @Override
    public String toString() {
        return "KitchenRequest{" +
                "strategy='" + strategy + '\'' +
                ", orderId=" + orderId +
                ", action='" + action + '\'' +
                '}';
    }
}