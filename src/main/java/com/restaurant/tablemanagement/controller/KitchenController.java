package com.restaurant.tablemanagement.controller;

import com.restaurant.tablemanagement.model.Order;
import com.restaurant.tablemanagement.service.KitchenService;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * KitchenController handles HTTP requests related to kitchen operations.
 * Provides REST endpoints for managing the kitchen queue and order processing.
 * 
 * Endpoints:
 * - GET /api/kitchen/next - Get the next order to prepare
 * - POST /api/kitchen/start/{orderId} - Start preparing an order
 * - POST /api/kitchen/ready/{orderId} - Mark an order as ready
 * - POST /api/kitchen/complete/{orderId} - Complete an order
 * - GET /api/kitchen/pending - Get all pending orders
 * - GET /api/kitchen/preparing - Get all preparing orders
 * - GET /api/kitchen/ready - Get all ready orders
 * - GET /api/kitchen/all - Get all orders in kitchen
 * - POST /api/kitchen/strategy/{strategy} - Set scheduling strategy
 * - GET /api/kitchen/statistics - Get queue statistics
 */
@RestController
@RequestMapping("/api/kitchen")
@CrossOrigin(origins = "*")
public class KitchenController {
    
    private final KitchenService kitchenService;
    
    /**
     * Constructor for dependency injection of KitchenService.
     * 
     * @param kitchenService The kitchen service
     */
    public KitchenController(KitchenService kitchenService) {
        this.kitchenService = kitchenService;
    }
    
    /**
     * Retrieves the next order to be prepared based on the current scheduling strategy.
     * 
     * @return ResponseEntity with the next order or a message if queue is empty
     */
    @GetMapping("/next")
    public ResponseEntity<?> getNextOrder() {
        Optional<Order> nextOrder = kitchenService.getNextOrderToProcess();
        
        if (nextOrder.isPresent()) {
            return ResponseEntity.ok(nextOrder.get());
        } else {
            return ResponseEntity.ok(new ApiResponse("No pending orders in the kitchen queue"));
        }
    }
    
    /**
     * Starts preparation of a specific order.
     * Moves the order from pending to preparing state.
     * 
     * @param orderId The ID of the order to start preparing
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/start/{orderId}")
    public ResponseEntity<ApiResponse> startPreparingOrder(@PathVariable String orderId) {
        boolean success = kitchenService.startPreparingOrder(orderId);
        
        if (success) {
            return ResponseEntity.ok(new ApiResponse("Order " + orderId + " preparation started"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse("Order not found or cannot be started"));
        }
    }
    
    /**
     * Marks an order as ready for delivery.
     * Moves the order from preparing to ready state.
     * 
     * @param orderId The ID of the order to mark as ready
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/ready/{orderId}")
    public ResponseEntity<ApiResponse> markOrderAsReady(@PathVariable String orderId) {
        boolean success = kitchenService.markOrderAsReady(orderId);
        
        if (success) {
            return ResponseEntity.ok(new ApiResponse("Order " + orderId + " is ready for delivery"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse("Order not found or not being prepared"));
        }
    }
    
    /**
     * Completes an order and removes it from the kitchen queue.
     * 
     * @param orderId The ID of the order to complete
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/complete/{orderId}")
    public ResponseEntity<ApiResponse> completeOrder(@PathVariable String orderId) {
        boolean success = kitchenService.completeOrder(orderId);
        
        if (success) {
            return ResponseEntity.ok(new ApiResponse("Order " + orderId + " completed"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse("Order not found or not ready"));
        }
    }
    
    /**
     * Retrieves all pending orders in the kitchen queue.
     * 
     * @return ResponseEntity with list of pending orders
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Order>> getPendingOrders() {
        return ResponseEntity.ok(kitchenService.getPendingOrders());
    }
    
    /**
     * Retrieves all orders currently being prepared.
     * 
     * @return ResponseEntity with list of preparing orders
     */
    @GetMapping("/preparing")
    public ResponseEntity<List<Order>> getPreparingOrders() {
        return ResponseEntity.ok(kitchenService.getPreparingOrders());
    }
    
    /**
     * Retrieves all orders ready for delivery.
     * 
     * @return ResponseEntity with list of ready orders
     */
    @GetMapping("/ready")
    public ResponseEntity<List<Order>> getReadyOrders() {
        return ResponseEntity.ok(kitchenService.getReadyOrders());
    }
    
    /**
     * Retrieves all orders currently in the kitchen.
     * 
     * @return ResponseEntity with list of all orders in kitchen
     */
    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrdersInKitchen() {
        return ResponseEntity.ok(kitchenService.getAllOrdersInKitchen());
    }
    
    /**
     * Sets the scheduling strategy for the kitchen queue.
     * Supports: FIFO, PRIORITY, SJF
     * 
     * @param strategyName The name of the scheduling strategy
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/strategy/{strategyName}")
    public ResponseEntity<ApiResponse> setSchedulingStrategy(@PathVariable String strategyName) {
        try {
            kitchenService.setSchedulingStrategyByName(strategyName);
            String currentStrategy = kitchenService.getCurrentSchedulingStrategy().getStrategyName();
            return ResponseEntity.ok(new ApiResponse("Scheduling strategy changed to: " + currentStrategy));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse("Invalid strategy. Use FIFO, PRIORITY, or SJF"));
        }
    }
    
    /**
     * Retrieves statistics about the current kitchen queue state.
     * 
     * @return ResponseEntity with queue statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<KitchenService.KitchenQueueStats> getQueueStatistics() {
        return ResponseEntity.ok(kitchenService.getQueueStatistics());
    }
    
    /**
     * Inner class for API response messages.
     */
    public static class ApiResponse {
        public String message;
        public long timestamp;
        
        public ApiResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getMessage() {
            return message;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
}