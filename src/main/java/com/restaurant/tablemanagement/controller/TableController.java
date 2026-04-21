package com.restaurant.tablemanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.restaurant.tablemanagement.service.RestaurantManager;
import com.restaurant.tablemanagement.service.OrderService;
import com.restaurant.tablemanagement.model.Table;
import com.restaurant.tablemanagement.model.Customer;
import com.restaurant.tablemanagement.dto.CustomerRequest;

@RestController
@RequestMapping("/tables")
public class TableController {

    @Autowired
    private RestaurantManager manager;

    @Autowired
    private OrderService orderService;

    @PostMapping("/allocate")
    public String allocate(@RequestBody CustomerRequest request) {

        // Convert DTO → Model
        Customer customer = new Customer(
                request.getCustomerId(),
                request.getGroupSize(),
                request.isVIP()
        );

        Table t = manager.allocateTable(customer);

        return t != null
                ? "Allocated Table " + t.getTableId()
                : "Added to waitlist";
    }

    @PostMapping("/free/{id}")
    public String free(@PathVariable int id) {
        int cancelledOrders = orderService.cancelActiveOrdersForTable("TABLE-" + id);
        manager.freeTable(id);
        return "Cleaning started. Cancelled orders: " + cancelledOrders;
    }

    @GetMapping
    public List<Table> getAll() {
        return manager.getTables();
    }
}