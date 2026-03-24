package com.restaurant.tablemanagement.controller;

import com.restaurant.tablemanagement.model.*;
import com.restaurant.tablemanagement.service.OrderService;
import com.restaurant.tablemanagement.dto.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    // ===== CREATE ORDER =====
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        try {
            Order order = orderService.createOrder(request);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // ===== GET ORDER BY ID =====
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable String orderId) {
        java.util.Optional<Order> order = orderService.getOrderById(orderId);
        if (order.isPresent()) {
            return ResponseEntity.ok(order.get());
        }
        return ResponseEntity.notFound().build();
    }
    
    // ===== GET ORDERS BY TABLE =====
    @GetMapping("/table/{tableId}")
    public ResponseEntity<List<Order>> getOrdersByTable(@PathVariable String tableId) {
        List<Order> orders = orderService.getOrdersByTable(tableId);
        return ResponseEntity.ok(orders);
    }
    
    // ===== GET ORDERS BY CUSTOMER =====
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomer(@PathVariable String customerId) {
        List<Order> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(orders);
    }
    
    // ===== GET ALL ACTIVE ORDERS =====
    @GetMapping("/active")
    public ResponseEntity<List<Order>> getActiveOrders() {
        List<Order> orders = orderService.getActiveOrders();
        return ResponseEntity.ok(orders);
    }
    
    // ===== GET ALL ORDERS =====
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
    
    // ===== ADD ITEM TO ORDER =====
    @PostMapping("/{orderId}/items")
    public ResponseEntity<Order> addItemToOrder(
            @PathVariable String orderId,
            @RequestBody OrderRequest.OrderItemRequest itemRequest) {
        try {
            Order order = orderService.addItemToOrder(orderId, itemRequest);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // ===== REMOVE ITEM FROM ORDER =====
    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<Order> removeItemFromOrder(
            @PathVariable String orderId,
            @PathVariable String itemId) {
        try {
            Order order = orderService.removeItemFromOrder(orderId, itemId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // ===== MODIFY ITEM QUANTITY =====
    @PutMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<Order> modifyItemQuantity(
            @PathVariable String orderId,
            @PathVariable String itemId,
            @RequestParam Integer newQuantity) {
        try {
            Order order = orderService.modifyItemQuantity(orderId, itemId, newQuantity);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // ===== CANCEL ORDER =====
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Order> cancelOrder(@PathVariable String orderId) {
        try {
            Order order = orderService.cancelOrder(orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // ===== UPDATE ORDER STATUS =====
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam OrderStatus status) {
        try {
            Order order = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // ===== VIEW PRIORITY QUEUE =====
    @GetMapping("/queue/view")
    public ResponseEntity<List<Order>> viewQueue() {
        List<Order> queue = orderService.getOrderQueue().viewQueue();
        return ResponseEntity.ok(queue);
    }
    
    // ===== RUSH HOUR MODE =====
    @PutMapping("/rush-hour")
    public ResponseEntity<String> setRushHourMode(@RequestParam boolean enabled) {
        orderService.enableRushHourMode(enabled);
        return ResponseEntity.ok("Rush hour mode: " + (enabled ? "ON" : "OFF"));
    }
    
    // ===== UPGRADE CUSTOMER TO VIP =====
    @PostMapping("/customer/{customerId}/vip")
    public ResponseEntity<String> upgradeCustomerToVIP(@PathVariable String customerId) {
        try {
            orderService.upgradeCustomerToVIP(customerId);
            return ResponseEntity.ok("✨ Customer " + customerId + " upgraded to VIP status!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error upgrading customer: " + e.getMessage());
        }
    }
    
    // ===== DOWNGRADE CUSTOMER FROM VIP =====
    @PostMapping("/customer/{customerId}/remove-vip")
    public ResponseEntity<String> downgradeCustomerFromVIP(@PathVariable String customerId) {
        try {
            orderService.downgradeCustomerFromVIP(customerId);
            return ResponseEntity.ok("Customer " + customerId + " VIP status removed.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error downgrading customer: " + e.getMessage());
        }
    }

    // ===== LOAD ORDERS UI =====
    @GetMapping("/orders")
    public String ordersPage() {
        return "orders";  // Returns orders.html
}
}

/*Purpose:
Exposes REST APIs for all order operations
Used by frontend and other systems
Handles HTTP requests/responses */