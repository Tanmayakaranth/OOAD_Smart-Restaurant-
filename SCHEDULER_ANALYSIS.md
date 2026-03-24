# Priority Queue & Scheduler Implementation Analysis

## Overview
This document provides a thorough analysis of the priority queue and scheduling system in the Smart Restaurant application, identifying the architecture, design patterns, and critical bugs.

---

## 1. Architecture Overview

### Strategy Design Pattern
The system implements the **Strategy Design Pattern** to allow runtime switching between different scheduling algorithms:

```
SchedulerStrategy (Interface)
    ├── FIFOScheduler
    ├── PriorityScheduler
    └── SJFScheduler

KitchenQueue (Context)
    └── Uses: SchedulerStrategy instance
```

---

## 2. SchedulerStrategy Interface

**File:** [src/main/java/com/restaurant/tablemanagement/service/SchedulerStrategy.java](src/main/java/com/restaurant/tablemanagement/service/SchedulerStrategy.java)

```java
public interface SchedulerStrategy {
    /**
     * Selects the next order to be processed based on the scheduling algorithm.
     * @param orders List of orders pending in the kitchen queue
     * @return The next Order to be processed, or null if no orders available
     */
    Order selectNextOrder(List<Order> orders);
    
    /**
     * Returns the name of the scheduling strategy.
     * @return String representation of the strategy name
     */
    String getStrategyName();
}
```

**Purpose:** Defines contract for all scheduling strategies

---

## 3. Scheduler Implementations

### 3.1 FIFOScheduler
**File:** [src/main/java/com/restaurant/tablemanagement/service/FIFOScheduler.java](src/main/java/com/restaurant/tablemanagement/service/FIFOScheduler.java)

```java
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
```

**Behavior:** Orders processed in arrival sequence (creation time)

**Sorting Logic in KitchenQueue:**
```java
pendingOrders.sort((o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()));
```

---

### 3.2 PriorityScheduler
**File:** [src/main/java/com/restaurant/tablemanagement/service/PriorityScheduler.java](src/main/java/com/restaurant/tablemanagement/service/PriorityScheduler.java)

```java
public class PriorityScheduler implements SchedulerStrategy {
    @Override
    public Order selectNextOrder(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return null;
        }
        // Queue is maintained in priority order by KitchenQueue.reorderPendingQueue()
        // VIP (1) > ONLINE (2) > NORMAL (3) > WALK_IN (4)
        return orders.get(0);
    }
    
    @Override
    public String getStrategyName() {
        return "Priority-Based Scheduling";
    }
}
```

**Behavior:** Higher priority orders processed first (VIP > ONLINE > NORMAL > WALK_IN)

**Sorting Logic in KitchenQueue:**
```java
pendingOrders.sort((o1, o2) -> {
    int priority1 = o1.getOrderPriority().getPriorityValue();
    int priority2 = o2.getOrderPriority().getPriorityValue();
    
    if (priority1 != priority2) {
        return Integer.compare(priority1, priority2);
    }
    // Same priority: process older orders first (FIFO)
    return o1.getCreatedAt().compareTo(o2.getCreatedAt());
});
```

---

### 3.3 SJFScheduler
**File:** [src/main/java/com/restaurant/tablemanagement/service/SJFScheduler.java](src/main/java/com/restaurant/tablemanagement/service/SJFScheduler.java)

```java
public class SJFScheduler implements SchedulerStrategy {
    @Override
    public Order selectNextOrder(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return null;
        }
        // Queue is maintained in item count order by KitchenQueue.reorderPendingQueue()
        // Orders with fewer items come first
        return orders.get(0);
    }
    
    @Override
    public String getStrategyName() {
        return "SJF (Shortest Job First)";
    }
}
```

**Behavior:** Orders with fewer items processed first (optimizes throughput)

**Sorting Logic in KitchenQueue:**
```java
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
```

---

## 4. KitchenQueue Service

**File:** [src/main/java/com/restaurant/tablemanagement/service/KitchenQueue.java](src/main/java/com/restaurant/tablemanagement/service/KitchenQueue.java)

### 4.1 Queue State Management

```java
public class KitchenQueue {
    private List<Order> pendingOrders;      // Awaiting preparation
    private List<Order> preparingOrders;    // Currently being prepared
    private List<Order> readyOrders;        // Ready for delivery
    private SchedulerStrategy schedulerStrategy;
    
    public KitchenQueue() {
        this.pendingOrders = new ArrayList<>();
        this.preparingOrders = new ArrayList<>();
        this.readyOrders = new ArrayList<>();
        this.schedulerStrategy = new FIFOScheduler();  // Default: FIFO
    }
}
```

**Order Workflow:**
```
CREATED (pendingOrders)
    ↓ startPreparation()
PREPARING (preparingOrders)
    ↓ markAsReady()
READY (readyOrders)
    ↓ completeOrder()
SERVED (removed from queue)
```

### 4.2 Adding Orders

```java
public void addOrder(Order order) {
    if (order != null) {
        order.setStatus(OrderStatus.CREATED);
        pendingOrders.add(order);
        // Re-sort immediately so the queue remains ordered by current strategy
        reorderPendingQueue();
    }
}
```

✅ **Correct:** Calls `reorderPendingQueue()` to maintain order

### 4.3 Setting Scheduling Strategy

```java
public void setSchedulerStrategy(SchedulerStrategy strategy) {
    if (strategy != null) {
        SchedulerStrategy oldStrategy = this.schedulerStrategy;
        this.schedulerStrategy = strategy;
        
        // Reorder pending orders based on new strategy
        reorderPendingQueue();
        
        String oldStrategyName = oldStrategy != null ? oldStrategy.getStrategyName() : "UNKNOWN";
        System.out.println("🔄 Scheduling strategy changed: " + oldStrategyName + " → " 
            + strategy.getStrategyName() + " | Reordered " + pendingOrders.size() 
            + " pending orders");
    }
}
```

✅ **Correct:** 
- Sets new strategy BEFORE reordering
- Calls `reorderPendingQueue()` immediately after change
- Logs strategy change with order count

### 4.4 Reordering Logic (CRITICAL FUNCTION)

```java
private void reorderPendingQueue() {
    if (pendingOrders == null || pendingOrders.isEmpty()) {
        return;
    }
    
    if (schedulerStrategy == null) {
        pendingOrders.sort((o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()));
        return;
    }
    
    // Sort based on the current strategy's logic
    if (schedulerStrategy instanceof PriorityScheduler) {
        pendingOrders.sort((o1, o2) -> {
            int priority1 = o1.getOrderPriority().getPriorityValue();
            int priority2 = o2.getOrderPriority().getPriorityValue();
            
            if (priority1 != priority2) {
                return Integer.compare(priority1, priority2);
            }
            return o1.getCreatedAt().compareTo(o2.getCreatedAt());
        });
    } else if (schedulerStrategy instanceof SJFScheduler) {
        pendingOrders.sort((o1, o2) -> {
            int itemCountComparison = Integer.compare(
                o1.getOrderItems().size(),
                o2.getOrderItems().size()
            );
            if (itemCountComparison == 0) {
                return o1.getCreatedAt().compareTo(o2.getCreatedAt());
            }
            return itemCountComparison;
        });
    } else if (schedulerStrategy instanceof FIFOScheduler) {
        pendingOrders.sort((o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()));
    } else {
        pendingOrders.sort((o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()));
    }
}
```

✅ **Correct:** Properly reorders based on current strategy

### 4.5 Getting Next Order

```java
public Optional<Order> getNextOrder() {
    Order nextOrder = schedulerStrategy.selectNextOrder(pendingOrders);
    return Optional.ofNullable(nextOrder);
}
```

✅ **Correct:** Uses strategy pattern to select next order

### 4.6 Moving Orders Through States

```java
public boolean startPreparation(Optional<Order> order) {
    if (order.isPresent()) {
        Order o = order.get();
        if (o != null && pendingOrders.contains(o)) {  // ⚠️ CRITICAL BUG HERE
            pendingOrders.remove(o);
            o.setStatus(OrderStatus.PREPARING);
            preparingOrders.add(o);
            return true;
        }
    }
    return false;
}
```

⚠️ **CRITICAL BUG:** See Section 6

---

## 5. KitchenService

**File:** [src/main/java/com/restaurant/tablemanagement/service/KitchenService.java](src/main/java/com/restaurant/tablemanagement/service/KitchenService.java)

### 5.1 Order Processing Methods

```java
@Service
public class KitchenService {
    private final KitchenQueue kitchenQueue;
    private final OrderService orderService;
    
    public KitchenService(OrderService orderService) {
        this.orderService = orderService;
        this.kitchenQueue = new KitchenQueue();
    }
    
    // Add order
    public void addOrderToKitchen(Order order) {
        if (order != null) {
            kitchenQueue.addOrder(order);
            if (webSocketController != null) {
                webSocketController.broadcastUpdate(KitchenUpdateMessage.orderAdded(order));
            }
        }
    }
    
    // Get next order
    public Optional<Order> getNextOrderToProcess() {
        return kitchenQueue.getNextOrder();
    }
    
    // Start preparation
    public boolean startPreparingOrder(String orderId) {
        Optional<Order> order = orderService.getOrderById(orderId);  // ⚠️ ISSUE HERE
        boolean result = kitchenQueue.startPreparation(order);      // ⚠️ ISSUE HERE
        
        if (result && order.isPresent() && webSocketController != null) {
            webSocketController.broadcastUpdate(KitchenUpdateMessage.orderStarted(order.get()));
        }
        return result;
    }
    
    // Similar methods for markOrderAsReady(), completeOrder()
}
```

⚠️ **ISSUE:** See Section 6

### 5.2 Strategy Switching

```java
public void setSchedulingStrategyByName(String strategyName) {
    SchedulerStrategy strategy;
    
    switch (strategyName.toUpperCase()) {
        case "PRIORITY":
            strategy = new PriorityScheduler();
            break;
        case "SJF":
            strategy = new SJFScheduler();
            break;
        case "FIFO":
        default:
            strategy = new FIFOScheduler();
            break;
    }
    
    setSchedulingStrategy(strategy);
}

public void setSchedulingStrategy(SchedulerStrategy strategy) {
    kitchenQueue.setSchedulerStrategy(strategy);
}
```

✅ **Correct:** Properly delegates to KitchenQueue

---

## 6. 🐛 CRITICAL BUGS IDENTIFIED

### BUG #1: Object Identity vs Equality in Order Lookup
**Severity:** 🔴 CRITICAL

**Location:** 
- [KitchenQueue.java](src/main/java/com/restaurant/tablemanagement/service/KitchenQueue.java#L59) - `startPreparation()`
- [KitchenQueue.java](src/main/java/com/restaurant/tablemanagement/service/KitchenQueue.java#L75) - `markAsReady()`
- [KitchenQueue.java](src/main/java/com/restaurant/tablemanagement/service/KitchenQueue.java#L88) - `completeOrder()`

**Problem:**
```java
public boolean startPreparation(Optional<Order> order) {
    if (order.isPresent()) {
        Order o = order.get();
        if (o != null && pendingOrders.contains(o)) {  // ⚠️ USES DEFAULT equals()
            // ... move to preparing
        }
    }
    return false;
}
```

**Root Cause:**
The `Order` class does NOT override `equals()` and `hashCode()`:

```java
public class Order {
    private String orderId;
    // ... other fields ...
    
    // ❌ NO equals() OR hashCode() IMPLEMENTATION
}
```

**Issue Flow:**
1. Order ABCD-123 added to `kitchenQueue` (stored as instance X)
2. User calls `startPreparingOrder("ABCD-123")`
3. `KitchenService.startPreparingOrder()` calls `orderService.getOrderById("ABCD-123")`
4. `OrderService` returns a NEW instance Y (same data, different object reference)
5. `kitchenQueue.startPreparation()` calls `pendingOrders.contains(Y)`
6. **Result:** `contains()` uses default `Object.equals()` (object identity check)
7. **X != Y** → `contains()` returns `false` → **Operation fails silently!**

**Impact:**
- ❌ Orders cannot move from PENDING → PREPARING (stuck in kitchen)
- ❌ Orders cannot move from PREPARING → READY (stuck in kitchen)
- ❌ Orders cannot move from READY → SERVED (stuck in kitchen)
- ❌ **Entire workflow breaks when orders are retrieved from database/service**

**Example Failure Scenario:**
```
Step 1: Order ORD-001 created → Added to pendingOrders [instance X]
Step 2: Try to start preparation with orderId="ORD-001"
Step 3: OrderService returns new Order instance with same data [instance Y]
Step 4: pendingOrders.contains(Y) → FALSE (X != Y by identity)
Step 5: Order remains in pending forever ❌
```

### BUG #2: Strategy Reordering Work Correctly, BUT...
**Severity:** 🟡 MEDIUM (Related to Bug #1)

**Location:** [KitchenService.java](src/main/java/com/restaurant/tablemanagement/service/KitchenService.java#L108-L120)

**Pattern:**
```java
public boolean startPreparingOrder(String orderId) {
    Optional<Order> order = orderService.getOrderById(orderId);
    boolean result = kitchenQueue.startPreparation(order);  // ⚠️ Object identity mismatch
    // ... rest of code
}
```

**Why This Compounds Bug #1:**
All state-changing operations use this pattern:
- `startPreparingOrder(orderId)` → retrieves from service → fails with new instance
- `markOrderAsReady(orderId)` → retrieves from service → fails with new instance
- `completeOrder(orderId)` → retrieves from service → fails with new instance

---

## 7. Solutions

### Solution #1: Override equals() and hashCode() in Order
**Priority:** 🔴 CRITICAL - Implement immediately

Add to [Order.java](src/main/java/com/restaurant/tablemanagement/model/Order.java):

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Order order = (Order) o;
    return Objects.equals(orderId, order.orderId);
}

@Override
public int hashCode() {
    return Objects.hash(orderId);
}
```

**Why:** `List.contains()` uses `equals()`, so orders will be found by ID match

### Solution #2: Alternative - Use Order ID Rather Than Object Reference
**Priority:** 🟡 MEDIUM - More robust approach

Modify KitchenQueue:
```java
public boolean startPreparation(String orderId) {
    Order order = pendingOrders.stream()
        .filter(o -> o.getOrderId().equals(orderId))
        .findFirst()
        .orElse(null);
    
    if (order != null) {
        pendingOrders.remove(order);
        order.setStatus(OrderStatus.PREPARING);
        preparingOrders.add(order);
        return true;
    }
    return false;
}
```

**Why:** Eliminates object identity dependency

### Solution #3: Implement toString() for Debugging
**Priority:** 🟢 OPTIONAL - Quality of life improvement

Add to Order.java:
```java
@Override
public String toString() {
    return "Order{" +
        "orderId='" + orderId + '\'' +
        ", orderPriority=" + orderPriority +
        ", orderStatus=" + orderStatus +
        ", itemCount=" + orderItems.size() +
        ", createdAt=" + createdAt +
        '}';
}
```

---

## 8. Queue Reordering When Strategy Changes

### How It Works (CORRECT IMPLEMENTATION):

**Scenario:** Change from FIFO to PRIORITY with 3 pending orders

**Initial State (FIFO):**
```
Order sequence by creation time:
  Order A (created 10:00) - NORMAL priority
  Order B (created 10:01) - VIP priority
  Order C (created 10:02) - NORMAL priority

FIFO Queue: [A, B, C]
```

**Step 1: Strategy Change Request**
```
Controller: POST /api/kitchen/strategy/PRIORITY
→ KitchenService.setSchedulingStrategyByName("PRIORITY")
→ KitchenQueue.setSchedulerStrategy(new PriorityScheduler())
```

**Step 2: Reordering (AUTOMATIC)**
```
setSchedulerStrategy() calls:
  ↓
reorderPendingQueue() with PriorityScheduler:
  - Sort by priority value (lower = higher)
  - VIP = 1, ONLINE = 2, NORMAL = 3, WALK_IN = 4
  - Orders B (1) > A (3) > C (3)
  - For same priority, use FIFO (creation time)
  
Result: [B(VIP:10:01), A(NORM:10:00), C(NORM:10:02)]
```

**Step 3: Next Order Selection**
```
getNextOrder():
→ PriorityScheduler.selectNextOrder([B,A,C])
→ Returns orders[0] = B (VIP order processed first!)
```

**Verification:**
```
GET /api/kitchen/next → Returns Order B (VIP) ✅
GET /api/kitchen/pending → Shows [B, A, C] ✅ (newly sorted)
```

---

## 9. The Missing Piece: Why Reordering Works (Even With Bug #1)

**Important Note:** Strategy changes and reordering work correctly BECAUSE:

```java
public void setSchedulerStrategy(SchedulerStrategy strategy) {
    // ... set new strategy ...
    reorderPendingQueue();  // ← Operates on pendingOrders List directly
}
```

The `reorderPendingQueue()` method:
- ✅ Operates on the internal `pendingOrders` list
- ✅ Reorders using internal references (no service calls)
- ✅ Uses List.sort() which maintains object identity

**Bug #1 appears when:** Orders try to MOVE between lists using service-retrieved references

---

## 10. Design Pattern Summary

### Pattern: Strategy Design Pattern
```
┌─────────────────┐
│ SchedulerStrategy (Interface)
│ + selectNextOrder(List<Order>): Order
└────────┬─────────┘
         │ implements
         ├─→ FIFOScheduler
         ├─→ PriorityScheduler
         └─→ SJFScheduler

┌─────────────────┐
│   KitchenQueue (Context)
│ - schedulerStrategy: SchedulerStrategy
│ - pendingOrders: List<Order>
│ + getNextOrder(): Optional<Order>
│ + setSchedulerStrategy(...)
└─────────────────┘
```

### Pattern: State Machine
```
CREATED (pendingOrders)
    ↓ startPreparation()
PREPARING (preparingOrders)
    ↓ markAsReady()
READY (readyOrders)
    ↓ completeOrder()
SERVED (removed)
```

---

## 11. Testing Recommendations

### Test Case 1: Object Identity Bug
```java
@Test
public void testOrderMovementWithServiceRetrieval() {
    // Create order and add to kitchen
    Order original = new Order("C1", "T1", OrderPriority.NORMAL);
    kitchenQueue.addOrder(original);
    
    // Simulate service retrieval (new instance, same data)
    Order retrieved = new Order("C1", "T1", OrderPriority.NORMAL);
    retrieved.setOrderId(original.getOrderId());
    
    // This should work but fails without equals() override
    boolean result = kitchenQueue.startPreparation(Optional.of(retrieved));
    
    // CURRENT: false ❌
    // EXPECTED: true ✅
    assertTrue(result);
}
```

### Test Case 2: Strategy Reordering
```java
@Test
public void testStrategyChangeReorders() {
    // Add orders in FIFO
    Order a = createOrder(1, OrderPriority.NORMAL);
    Order b = createOrder(2, OrderPriority.VIP);
    Order c = createOrder(3, OrderPriority.NORMAL);
    
    kitchenQueue.addOrder(a);
    kitchenQueue.addOrder(b);
    kitchenQueue.addOrder(c);
    
    // Initial FIFO order: [a, b, c]
    
    // Change to PRIORITY
    kitchenQueue.setSchedulerStrategy(new PriorityScheduler());
    
    // Expected priority order: [b(VIP), a(NORMAL), c(NORMAL)]
    assertEquals(b, kitchenQueue.getNextOrder().get());
}
```

---

## 12. Code Quality Issues (Additional)

### Issue 1: Nullable OrderService
```java
@Autowired(required = false)
private KitchenWebSocketController webSocketController;
```

Should add null checks, which the code does. ✅

### Issue 2: Mock Data in Production
```java
// In Order.updateTotal()
totalAmount = orderItems.stream()
    .mapToDouble(i -> i.getQuantity() * 100.0)  // ⚠️ MOCK PRICE
    .sum();
```

Should integrate with MenuItem service for real prices.

---

## 13. Summary

| Component | Status | Issues |
|-----------|--------|--------|
| SchedulerStrategy (Interface) | ✅ Good | None |
| FIFOScheduler | ✅ Good | None |
| PriorityScheduler | ✅ Good | None |
| SJFScheduler | ✅ Good | None |
| KitchenQueue.reorderPendingQueue() | ✅ Good | None |
| KitchenQueue.setSchedulerStrategy() | ✅ Good | Strategy switching works correctly |
| KitchenQueue.startPreparation/markAsReady/completeOrder | 🔴 BROKEN | Object identity bug (Bug #1) |
| KitchenService | 🔴 BROKEN | Passes wrong object reference (Bug #1) |
| Order.equals() | ❌ MISSING | ROOT CAUSE - No equals/hashCode override |

---

## 14. Recommended Immediate Actions

1. **URGENT:** Add `equals()` and `hashCode()` to Order model
2. **HIGH:** Add unit tests for order state transitions
3. **HIGH:** Test strategy changes with actual orders in queue
4. **MEDIUM:** Consider using orderId instead of object reference in KitchenQueue methods
5. **MEDIUM:** Add integration tests with OrderService

