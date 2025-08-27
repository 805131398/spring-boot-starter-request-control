package com.luckcoder.request.control.service;

import com.luckcoder.request.control.config.RequestControlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        // 如果配置了静态密钥，使用静态密钥验证（向后兼容）
        if (properties.getSecretKey() != null && !properties.getSecretKey().equals("dynamic")) {
            return Objects.equals(properties.getSecretKey(), secretKey);
        }
        
        // 使用动态时间密钥验证：格式为 MMHHDD（分钟小时日）
        return validateDynamicTimeKey(secretKey);
    }
    
    /**
     * 验证动态时间密钥
     * 密钥格式：MMHHDD（分钟小时日）
     * 例如：2025-08-27 10:43:30 → 密钥为 431027
     * 
     * @param secretKey 用户提供的密钥
     * @return 是否验证成功
     */
    private boolean validateDynamicTimeKey(String secretKey) {
        if (secretKey == null || secretKey.length() != 6) {
            logger.warn("Dynamic time key validation failed: invalid format, expected 6 digits but got: {}", 
                       secretKey == null ? "null" : secretKey.length() + " digits");
            return false;
        }
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 获取当前时间的各个部分
            int currentMinute = now.getMinute();
            int currentHour = now.getHour();
            int currentDay = now.getDayOfMonth();
            
            // 构建期望的密钥：MMHHDD 格式
            String expectedKey = String.format("%02d%02d%02d", currentMinute, currentHour, currentDay);
            
            boolean isValid = expectedKey.equals(secretKey);
            
            if (isValid) {
                logger.info("Dynamic time key validation successful: {} at {}", 
                           secretKey, now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                logger.warn("Dynamic time key validation failed: expected {} but got {} at {}", 
                           expectedKey, secretKey, now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("Error validating dynamic time key: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 生成当前时间的动态密钥
     * 主要用于测试和调试，也可以供客户端生成密钥使用
     * 
     * @return 当前时间对应的动态密钥（MMHHDD格式）
     */
    public static String generateCurrentTimeKey() {
        LocalDateTime now = LocalDateTime.now();
        int currentMinute = now.getMinute();
        int currentHour = now.getHour();
        int currentDay = now.getDayOfMonth();
        return String.format("%02d%02d%02d", currentMinute, currentHour, currentDay);
    }
    
    /**
     * 生成指定时间的动态密钥
     * 
     * @param dateTime 指定时间
     * @return 指定时间对应的动态密钥（MMHHDD格式）
     */
    public static String generateTimeKey(LocalDateTime dateTime) {
        int minute = dateTime.getMinute();
        int hour = dateTime.getHour();
        int day = dateTime.getDayOfMonth();
        return String.format("%02d%02d%02d", minute, hour, day);
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