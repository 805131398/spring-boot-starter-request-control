package com.luckcoder.request.control.controller;

import com.luckcoder.request.control.config.RequestControlProperties;
import com.luckcoder.request.control.dto.ApiResponse;
import com.luckcoder.request.control.dto.RequestStatusInfo;
import com.luckcoder.request.control.service.RequestControlService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("${request.control.control-path:/set-request}")
public class RequestControlController {
    
    private final RequestControlService requestControlService;
    private final RequestControlProperties properties;
    
    public RequestControlController(RequestControlService requestControlService,
                                  RequestControlProperties properties) {
        this.requestControlService = requestControlService;
        this.properties = properties;
    }
    
    @GetMapping("/{enabled}/{secretKey}")
    public ApiResponse<RequestStatusInfo> setRequestStatus(
            @PathVariable boolean enabled,
            @PathVariable String secretKey) {
        
        if (requestControlService.setRequestEnabled(enabled, secretKey)) {
            RequestStatusInfo info = new RequestStatusInfo(
                enabled, 
                Instant.now(),
                "api"
            );
            return ApiResponse.success(info, 
                String.format("Request processing %s successfully", 
                    enabled ? "enabled" : "disabled"));
        } else {
            return ApiResponse.error(401, "Invalid secret key");
        }
    }
    
    @GetMapping("/status/{secretKey}")
    public ApiResponse<RequestStatusInfo> getStatus(@PathVariable String secretKey) {
        if (!validateSecretKey(secretKey)) {
            return ApiResponse.error(401, "Invalid secret key");
        }
        
        RequestStatusInfo info = new RequestStatusInfo(
            requestControlService.isRequestEnabled(),
            Instant.now(),
            "query"
        );
        return ApiResponse.success(info, "Status retrieved successfully");
    }
    
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("available", true);
        info.put("version", "1.0.0");
        info.put("timestamp", Instant.now());
        info.put("enabled", requestControlService.isRequestEnabled());
        return ApiResponse.success(info, "Service information");
    }
    
    private boolean validateSecretKey(String secretKey) {
        return Objects.equals(properties.getSecretKey(), secretKey);
    }
}