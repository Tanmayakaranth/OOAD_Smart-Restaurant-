package com.restaurant.billing.service;
import com.restaurant.billing.observer.Observer;
import com.restaurant.billing.model.Bill;
import com.restaurant.tablemanagement.model.Order;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class BillingService {

    private List<Bill> bills = new ArrayList<>();
    private List<Observer> observers = new ArrayList<>();

    public Bill generateBill(Order order) {

        double total = order.getTotalAmount();

        double tax = total * 0.10;
        double discount = total * 0.05;

        double finalAmount = total + tax - discount;

        Bill bill = new Bill(order.getOrderId(), total, tax, discount, finalAmount);

        bills.add(bill);
        System.out.println("✅ Bill generated for Order: " + order.getOrderId());

        notifyObservers(bill);


        return bill;

    }

    public List<Bill> getAllBills() {
        return bills;
    }

    public void addObserver(Observer observer) {
    observers.add(observer);
    }

    private void notifyObservers(Bill bill) {
        for (Observer observer : observers) {
            observer.update(bill);
        }
    }
}