package com.restaurant.billing.controller;

import com.restaurant.billing.service.BillingService;
import com.restaurant.billing.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@Controller
public class BillingController {

    private BillingService billingService;
    private DashboardService dashboardService;

    public BillingController(BillingService billingService, DashboardService dashboardService) {
        this.billingService = billingService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/billing")
    public String showBillingPage(Model model) {

        model.addAttribute("bills", billingService.getAllBills());
        model.addAttribute("totalRevenue", dashboardService.getTotalRevenue());
        model.addAttribute("totalBills", dashboardService.getTotalBills());

        return "billing";
    }

    @GetMapping("/api/billing/data")
    @ResponseBody
    public Map<String, Object> getBillingData() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalRevenue", dashboardService.getTotalRevenue());
        data.put("totalBills", dashboardService.getTotalBills());
        data.put("bills", billingService.getAllBills());
        return data;
    }
}