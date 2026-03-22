package com.restaurant.tablemanagement.service;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;

import com.restaurant.tablemanagement.model.Table;
import com.restaurant.tablemanagement.model.Customer;
import com.restaurant.tablemanagement.model.TableStatus;

@Service
public class RestaurantManager {

    private final List<Table> tables = new ArrayList<>();

    // 🔥 PRIORITY: VIP → EARLIER ARRIVAL
    private final PriorityQueue<Customer> waitlist =
            new PriorityQueue<>((a, b) -> {
                if (a.isVIP() != b.isVIP()) {
                    return Boolean.compare(b.isVIP(), a.isVIP());
                }
                return Long.compare(a.getArrivalTime(), b.getArrivalTime());
            });

    public RestaurantManager() {
        tables.add(new Table(1, 2));
        tables.add(new Table(2, 4));
        tables.add(new Table(3, 4));
        tables.add(new Table(4, 6));
    }

    // Allocate table (best-fit)
    public synchronized Table allocateTable(Customer customer) {
        Table best = findBestFit(customer.getGroupSize());

        if (best != null) {
            best.setStatus(TableStatus.OCCUPIED);
            return best;
        }

        waitlist.add(customer);
        return null;
    }

    // Free table → start cleaning
    public synchronized void freeTable(int id) {
        for (Table t : tables) {
            if (t.getTableId() == id) {
                t.setStatus(TableStatus.CLEANING);
                t.setCleaningEndTime(System.currentTimeMillis() + 5000);

                System.out.println("Table " + id + " is CLEANING");
                break;
            }
        }
    }

    // Scheduler: runs every second
    @Scheduled(fixedRate = 1000)
    public synchronized void autoClean() {
        long now = System.currentTimeMillis();

        for (Table t : tables) {
            if (t.getStatus() == TableStatus.CLEANING &&
                now >= t.getCleaningEndTime()) {

                System.out.println("Table " + t.getTableId() + " finished cleaning");

                // Step 1: Available
                t.setStatus(TableStatus.AVAILABLE);
                t.setCleaningEndTime(0);

                // Step 2: Assign best customer
                assignBestCustomer(t);
            }
        }
    }

    // 🔥 Assign based on VIP + waiting time + capacity
    private void assignBestCustomer(Table table) {
        if (waitlist.isEmpty()) return;

        List<Customer> candidates = new ArrayList<>();

        // Filter customers that can fit
        for (Customer c : waitlist) {
            if (table.getCapacity() >= c.getGroupSize()) {
                candidates.add(c);
            }
        }

        if (candidates.isEmpty()) return;

        // Sort: VIP → earliest arrival
        candidates.sort((a, b) -> {
            if (a.isVIP() != b.isVIP()) {
                return Boolean.compare(b.isVIP(), a.isVIP());
            }
            return Long.compare(a.getArrivalTime(), b.getArrivalTime());
        });

        Customer chosen = candidates.get(0);

        table.setStatus(TableStatus.OCCUPIED);
        waitlist.remove(chosen);

        System.out.println("Assigned Table " + table.getTableId() +
                " to Customer " + chosen.getCustomerId());
    }

    // Best-fit table selection
    private Table findBestFit(int groupSize) {
        Table best = null;

        for (Table t : tables) {
            if (t.getStatus() == TableStatus.AVAILABLE &&
                t.getCapacity() >= groupSize) {

                if (best == null || t.getCapacity() < best.getCapacity()) {
                    best = t;
                }
            }
        }

        return best;
    }

    public List<Table> getTables() {
        return tables;
    }

    public List<Customer> getWaitlist() {
        return new ArrayList<>(waitlist);
    }
}