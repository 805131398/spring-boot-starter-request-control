package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/api/hello")
    public String hello() {
        return "Hello from Spring Boot Starter Request Control Demo!";
    }

    @GetMapping("/api/users")
    public List<Map<String, Object>> getUsers() {
        return Arrays.asList(
            createUser("张三", 25, "developer"),
            createUser("李四", 30, "manager"),
            createUser("王五", 28, "designer")
        );
    }

    @GetMapping("/api/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "demo-app");
        status.put("version", "1.0.0");
        status.put("status", "running");
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        return health;
    }

    private Map<String, Object> createUser(String name, int age, String role) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("age", age);
        user.put("role", role);
        return user;
    }
}