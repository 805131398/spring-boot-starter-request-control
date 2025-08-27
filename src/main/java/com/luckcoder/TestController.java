package com.luckcoder;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {
    
    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Test API works!");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }
}