package com.restaurant.tablemanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

import com.restaurant.tablemanagement.service.RestaurantManager;
import com.restaurant.tablemanagement.service.OrderService;
import com.restaurant.tablemanagement.service.KitchenService;
import com.restaurant.tablemanagement.model.Customer;
import com.restaurant.tablemanagement.model.Table;

@Controller
public class ViewController {

    @Autowired
    private RestaurantManager manager;

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private KitchenService kitchenService;

    // 🔹 Load main UI
    @GetMapping("/")
    public String viewTables(Model model) {
        model.addAttribute("tables", manager.getTables());
        model.addAttribute("waitlist", manager.getWaitlist());
        return "tables";
    }

    // 🔹 Load orders UI
    @GetMapping({"/orders", "/orders.html"})
    public String viewOrders() {
        return "orders";
    }

    // 🔹 Load kitchen dashboard UI
    @GetMapping({"/kitchen", "/kitchen.html"})
    public String viewKitchen(Model model) {
        model.addAttribute("pendingOrders", kitchenService.getPendingOrders());
        return "kitchen";
    }

    // 🔹 Allocate table
    @PostMapping("/allocate")
    public String allocate(
            @RequestParam int customerId,
            @RequestParam int groupSize,
            @RequestParam(required = false) boolean isVIP,
            Model model) {

        Customer customer = new Customer(customerId, groupSize, isVIP);
        Table table = manager.allocateTable(customer);

        if (table != null) {
            model.addAttribute("message", "Allocated Table " + table.getTableId());
        } else {
            model.addAttribute("message", "Added to waitlist");
        }

        model.addAttribute("tables", manager.getTables());
        model.addAttribute("waitlist", manager.getWaitlist());

        return "tables";
    }

    // 🔹 Free table
    @PostMapping("/free/{id}")
    public String free(@PathVariable int id, Model model) {
        int cancelledOrders = orderService.cancelActiveOrdersForTable("TABLE-" + id);
        manager.freeTable(id);

        model.addAttribute("message", "Table " + id + " is being cleaned. Cancelled orders: " + cancelledOrders);
        model.addAttribute("tables", manager.getTables());
        model.addAttribute("waitlist", manager.getWaitlist());

        return "tables";
    }

    // 🔥 API for real-time UI (VERY IMPORTANT)
    @GetMapping("/tables-data")
    @ResponseBody
    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("tables", manager.getTables());
        data.put("waitlist", manager.getWaitlist());
        return data;
    }
}