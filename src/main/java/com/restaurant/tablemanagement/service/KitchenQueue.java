package com.restaurant.tablemanagement.service;

import com.restaurant.billing.service.BillingService;
import com.restaurant.billing.service.DashboardService;
import com.restaurant.tablemanagement.model.Order;
import com.restaurant.tablemanagement.model.OrderStatus;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class KitchenQueue {

    private List<Order> pendingOrders;
    private List<Order> preparingOrders;
    private List<Order> readyOrders;
    private SchedulerStrategy schedulerStrategy;

    private final BillingService billingService;
    private final DashboardService dashboardService;

    // ✅ Constructor Injection (IMPORTANT)
    public KitchenQueue(BillingService billingService, DashboardService dashboardService) {
        this.pendingOrders = new ArrayList<>();
        this.preparingOrders = new ArrayList<>();
        this.readyOrders = new ArrayList<>();
        this.schedulerStrategy = new FIFOScheduler();

        this.billingService = billingService;
        this.dashboardService = dashboardService;

        // ✅ Connect Observer
        this.billingService.addObserver(this.dashboardService);
    }

    public void addOrder(Order order) {
        if (order != null) {
            order.setStatus(OrderStatus.CREATED);
            pendingOrders.add(order);
            reorderPendingQueue();
        }
    }

    public Optional<Order> getNextOrder() {
        Order nextOrder = schedulerStrategy.selectNextOrder(pendingOrders);
        return Optional.ofNullable(nextOrder);
    }

    public boolean startPreparation(Optional<Order> order) {
        if (order.isPresent()) {
            Order o = order.get();
            if (pendingOrders.contains(o)) {
                pendingOrders.remove(o);
                o.setStatus(OrderStatus.PREPARING);
                preparingOrders.add(o);
                return true;
            }
        }
        return false;
    }

    public boolean markAsReady(Optional<Order> order) {
        if (order.isPresent()) {
            Order o = order.get();
            if (preparingOrders.contains(o)) {
                preparingOrders.remove(o);
                o.setStatus(OrderStatus.READY);
                readyOrders.add(o);
                return true;
            }
        }
        return false;
    }

    public boolean completeOrder(Optional<Order> order) {
        if (order.isPresent()) {
            Order o = order.get();
            if (readyOrders.contains(o)) {
                readyOrders.remove(o);
                o.setStatus(OrderStatus.SERVED);

                // ✅ BILLING TRIGGER
                billingService.generateBill(o);

                return true;
            }
        }
        return false;
    }

    private void reorderPendingQueue() {
        pendingOrders.sort((o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()));
    }

    // ✅ Missing getter methods
    public void setSchedulerStrategy(SchedulerStrategy strategy) {
        if (strategy != null) {
            this.schedulerStrategy = strategy;
        }
    }

    public SchedulerStrategy getSchedulerStrategy() {
        return this.schedulerStrategy;
    }

    public List<Order> getPendingOrders() {
        return new ArrayList<>(pendingOrders);
    }

    public List<Order> getPreparingOrders() {
        return new ArrayList<>(preparingOrders);
    }

    public List<Order> getReadyOrders() {
        return new ArrayList<>(readyOrders);
    }

    public List<Order> getAllOrdersInKitchen() {
        List<Order> allOrders = new ArrayList<>();
        allOrders.addAll(pendingOrders);
        allOrders.addAll(preparingOrders);
        allOrders.addAll(readyOrders);
        return allOrders;
    }

    public int getPendingOrderCount() {
        return pendingOrders.size();
    }

    public int getPreparingOrderCount() {
        return preparingOrders.size();
    }

    public int getReadyOrderCount() {
        return readyOrders.size();
    }
}