package com.restaurant.tablemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // ✅ Enables auto-cleaning scheduler
public class TableManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TableManagementApplication.class, args);
    }
}