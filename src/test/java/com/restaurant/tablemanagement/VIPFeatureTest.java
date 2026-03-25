// filepath: VIPFeatureTest.java
package com.restaurant.tablemanagement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import com.restaurant.tablemanagement.model.*;
import com.restaurant.tablemanagement.service.RestaurantManager;
import com.restaurant.tablemanagement.service.OrderService;
import com.restaurant.tablemanagement.service.OrderPriorityQueue;
import com.restaurant.tablemanagement.dto.OrderRequest;
import java.util.List;

public class VIPFeatureTest {
    
    private RestaurantManager manager;
    private OrderService orderService;
    private OrderPriorityQueue queue;
    
    @BeforeEach
    public void setup() {
        manager = new RestaurantManager();
        orderService = new OrderService();
        orderService.setRestaurantManager(manager);  // Inject manager dependency
        queue = orderService.getOrderQueue();
    }
    
    // ===== TEST 1: Table marked as VIP when VIP customer allocated =====
    @Test
    public void testTableMarkedVIPWhenVIPCustomerAllocated() {
        System.out.println("\n✅ TEST 1: Table marked VIP when VIP customer allocated");
        
        Customer vipCustomer = new Customer(100, 4, true);  // isVIP = true
        Table allocatedTable = manager.allocateTable(vipCustomer);
        
        assertNotNull(allocatedTable, "Table should be allocated");
        assertTrue(allocatedTable.isVIP(), "Table should be marked as VIP");
        System.out.println("✓ Table " + allocatedTable.getTableId() + " marked as VIP");
    }
    
    // ===== TEST 2: Table NOT marked as VIP for non-VIP customer =====
    @Test
    public void testTableNotVIPForNonVIPCustomer() {
        System.out.println("\n✅ TEST 2: Table NOT marked VIP for non-VIP customer");
        
        Customer regularCustomer = new Customer(200, 2, false);  // isVIP = false
        Table allocatedTable = manager.allocateTable(regularCustomer);
        
        assertNotNull(allocatedTable, "Table should be allocated");
        assertFalse(allocatedTable.isVIP(), "Table should NOT be marked as VIP");
        System.out.println("✓ Table " + allocatedTable.getTableId() + " is regular (not VIP)");
    }
    
    // ===== TEST 3: Order gets VIP priority when created from VIP table =====
    @Test
    public void testOrderGetVIPPriorityFromVIPTable() {
        System.out.println("\n✅ TEST 3: Order gets VIP priority from VIP table");
        
        // Step 1: Allocate VIP customer
        Customer vipCustomer = new Customer(100, 4, true);
        Table vipTable = manager.allocateTable(vipCustomer);
        assertTrue(vipTable.isVIP(), "Setup: VIP table should be marked");
        
        // Step 2: Create order without specifying priority (should auto-set to VIP)
        OrderRequest request = new OrderRequest();
        request.setTableId("TABLE-" + vipTable.getTableId());
        request.setCustomerId("CUST-100");
        request.setOrderPriority(null);  // No priority specified!
        
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest();
        item.setMenuItemId("MENU-001");
        item.setQuantity(2);
        request.setItems(List.of(item));
        
        Order order = orderService.createOrder(request);
        
        assertNotNull(order, "Order should be created");
        assertEquals(OrderPriority.VIP, order.getOrderPriority(), 
                     "Order should have VIP priority auto-set from VIP table");
        System.out.println("✓ Order " + order.getOrderId() + " has VIP priority: " + order.getOrderPriority());
    }
    
    // ===== TEST 4: Order gets NORMAL priority when created from regular table =====
    @Test
    public void testOrderGetNORMALPriorityFromRegularTable() {
        System.out.println("\n✅ TEST 4: Order gets NORMAL priority from regular table");
        
        // Step 1: Allocate regular customer
        Customer regularCustomer = new Customer(200, 2, false);
        Table regularTable = manager.allocateTable(regularCustomer);
        assertFalse(regularTable.isVIP(), "Setup: Regular table should NOT be VIP");
        
        // Step 2: Create order without specifying priority
        OrderRequest request = new OrderRequest();
        request.setTableId("TABLE-" + regularTable.getTableId());
        request.setCustomerId("CUST-200");
        request.setOrderPriority(null);  // No priority specified!
        
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest();
        item.setMenuItemId("MENU-001");
        item.setQuantity(1);
        request.setItems(List.of(item));
        
        Order order = orderService.createOrder(request);
        
        assertNotNull(order, "Order should be created");
        assertEquals(OrderPriority.NORMAL, order.getOrderPriority(), 
                     "Order should have NORMAL priority from regular table");
        System.out.println("✓ Order " + order.getOrderId() + " has NORMAL priority: " + order.getOrderPriority());
    }
    
    // ===== TEST 5: VIP order appears first in priority queue =====
    @Test
    public void testVIPOrderAppearFirstInQueue() {
        System.out.println("\n✅ TEST 5: VIP order appears first in priority queue");
        
        // Step 1: Create NORMAL order first
        Customer normalCustomer = new Customer(300, 2, false);
        Table normalTable = manager.allocateTable(normalCustomer);
        
        OrderRequest normalRequest = new OrderRequest();
        normalRequest.setTableId("TABLE-" + normalTable.getTableId());
        normalRequest.setCustomerId("CUST-300");
        normalRequest.setOrderPriority(OrderPriority.NORMAL);
        OrderRequest.OrderItemRequest item1 = new OrderRequest.OrderItemRequest();
        item1.setMenuItemId("MENU-001");
        item1.setQuantity(1);
        normalRequest.setItems(List.of(item1));
        
        Order normalOrder = orderService.createOrder(normalRequest);
        System.out.println("  - Created NORMAL order: " + normalOrder.getOrderId());
        
        // Step 2: Create VIP order after
        Customer vipCustomer = new Customer(100, 4, true);
        Table vipTable = manager.allocateTable(vipCustomer);
        
        OrderRequest vipRequest = new OrderRequest();
        vipRequest.setTableId("TABLE-" + vipTable.getTableId());
        vipRequest.setCustomerId("CUST-100");
        vipRequest.setOrderPriority(null);  // Should auto-set to VIP
        OrderRequest.OrderItemRequest item2 = new OrderRequest.OrderItemRequest();
        item2.setMenuItemId("MENU-002");
        item2.setQuantity(2);
        vipRequest.setItems(List.of(item2));
        
        Order vipOrder = orderService.createOrder(vipRequest);
        System.out.println("  - Created VIP order: " + vipOrder.getOrderId());
        
        // Step 3: Check queue order
        List<Order> queueOrders = queue.viewQueue();
        assertFalse(queueOrders.isEmpty(), "Queue should not be empty");
        
        Order firstInQueue = queueOrders.get(0);
        assertEquals(OrderPriority.VIP, firstInQueue.getOrderPriority(), 
                     "VIP order should be FIRST in queue");
        assertEquals(vipOrder.getOrderId(), firstInQueue.getOrderId(), 
                     "VIP order ID should match");
        System.out.println("✓ Queue order: VIP order is FIRST (priority: " + firstInQueue.getOrderPriority() + ")");
        System.out.println("  Queue size: " + queueOrders.size() + " orders");
    }
    
    // ===== TEST 6: VIP flag cleared when table freed =====
    @Test
    public void testVIPFlagClearedWhenTableFreed() {
        System.out.println("\n✅ TEST 6: VIP flag cleared when table freed");
        
        // Step 1: Allocate VIP customer
        Customer vipCustomer = new Customer(100, 4, true);
        Table vipTable = manager.allocateTable(vipCustomer);
        int tableId = vipTable.getTableId();
        assertTrue(vipTable.isVIP(), "Setup: Table should be VIP");
        
        // Step 2: Free the table
        manager.freeTable(tableId);
        
        // Step 3: Check VIP flag after table is freed
        Table freedTable = manager.getTableById(tableId);
        assertNotNull(freedTable, "Table should still exist");
        assertFalse(freedTable.isVIP(), "VIP flag should be cleared after table is freed");
        System.out.println("✓ Table " + tableId + " VIP flag cleared after free");
    }
}

/*
RUN TESTS:
mvn test -Dtest=VIPFeatureTest

Expected output:
✅ TEST 1: Table marked VIP when VIP customer allocated
✅ TEST 2: Table NOT marked VIP for non-VIP customer  
✅ TEST 3: Order gets VIP priority from VIP table
✅ TEST 4: Order gets NORMAL priority from regular table
✅ TEST 5: VIP order appears first in priority queue
✅ TEST 6: VIP flag cleared when table freed
*/
