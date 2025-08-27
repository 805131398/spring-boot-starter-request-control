package com.luckcoder.request.control.service;

import com.luckcoder.request.control.config.RequestControlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class RequestControlService {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestControlService.class);
    
    private final AtomicBoolean requestEnabled;
    private final RequestControlProperties properties;
    private final AntPathMatcher pathMatcher;
    
    public RequestControlService(RequestControlProperties properties) {
        this.properties = properties;
        this.requestEnabled = new AtomicBoolean(properties.isDefaultEnabled());
        this.pathMatcher = new AntPathMatcher();
        
        logger.info("RequestControlService initialized with default enabled: {}", 
                   properties.isDefaultEnabled());
    }
    
    public boolean setRequestEnabled(boolean enabled, String secretKey) {
        if (!validateSecretKey(secretKey)) {
            logger.warn("Invalid secret key attempt: {}", secretKey);
            return false;
        }
        
        boolean oldValue = this.requestEnabled.getAndSet(enabled);
        logger.info("Request status changed from {} to {} by secret key", oldValue, enabled);
        return true;
    }
    
    public boolean isRequestEnabled() {
        return requestEnabled.get();
    }
    
    private boolean validateSecretKey(String secretKey) {
        return Objects.equals(properties.getSecretKey(), secretKey);
    }
    
    public boolean isWhitelistPath(String path) {
        if (path == null) {
            return false;
        }
        
        return properties.getWhitelistPaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
    
    public boolean isControlPath(String path) {
        if (path == null) {
            return false;
        }
        
        String controlPattern = properties.getControlPath() + "/**";
        return pathMatcher.match(controlPattern, path);
    }
}