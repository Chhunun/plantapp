package com.plantapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This is the main entry point for the Spring Boot web application.
 * The @SpringBootApplication annotation enables auto-configuration,
 * component scanning, and starts the embedded web server.
 */
@SpringBootApplication
public class PlantWebAppApplication {

    public static void main(String[] args) {
        // This single line starts the entire web application.
        SpringApplication.run(PlantWebAppApplication.class, args);
        System.out.println("\n--- Web Server Started! ---");
        System.out.println("Access your application at: http://localhost:8080");
    }
}