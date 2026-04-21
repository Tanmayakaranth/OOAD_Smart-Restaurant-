package com.restaurant.billing.observer;

import com.restaurant.billing.model.Bill;

public interface Observer {
    void update(Bill bill);
}