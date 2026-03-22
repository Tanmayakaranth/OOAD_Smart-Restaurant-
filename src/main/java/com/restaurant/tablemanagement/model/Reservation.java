package com.restaurant.tablemanagement.model;

public class Reservation {

    private Customer customer;
    private int tableId;
    private long reservationTime;

    public Reservation(Customer customer, int tableId, long reservationTime) {
        this.customer = customer;
        this.tableId = tableId;
        this.reservationTime = reservationTime;
    }

    public Customer getCustomer() {
        return customer;
    }

    public int getTableId() {
        return tableId;
    }

    public long getReservationTime() {
        return reservationTime;
    }
}