package com.restaurant.billing.service;

import com.restaurant.billing.model.Bill;
import com.restaurant.billing.observer.Observer;
import org.springframework.stereotype.Service;

@Service
public class DashboardService implements Observer {

    private double totalRevenue = 0;
    private int totalBills = 0;

    @Override
    public void update(Bill bill) {
        totalRevenue += bill.getFinalAmount();
        totalBills++;

        System.out.println("📊 Dashboard Updated:");
        System.out.println("Total Revenue: " + totalRevenue);
        System.out.println("Total Bills: " + totalBills);
    }

    public double getTotalRevenue() {
    return totalRevenue;
    }

    public int getTotalBills() {
        return totalBills;
    }
}