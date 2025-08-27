package com.luckcoder.request.control.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckcoder.request.control.config.RequestControlProperties;
import com.luckcoder.request.control.dto.ApiResponse;
import com.luckcoder.request.control.service.RequestControlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

@Component
public class RequestControlInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestControlInterceptor.class);
    
    private final RequestControlService requestControlService;
    private final RequestControlProperties properties;
    private final ObjectMapper objectMapper;
    
    public RequestControlInterceptor(RequestControlService requestControlService,
                                   RequestControlProperties properties,
                                   ObjectMapper objectMapper) {
        this.requestControlService = requestControlService;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        
        if (!properties.isEnabled()) {
            return true;
        }
        
        String requestPath = request.getRequestURI();
        
        if (requestControlService.isWhitelistPath(requestPath)) {
            return true;
        }
        
        if (!requestControlService.isRequestEnabled()) {
            handleRejectedRequest(request, response);
            return false;
        }
        
        return true;
    }
    
    private void handleRejectedRequest(HttpServletRequest request, 
                                     HttpServletResponse response) throws Exception {
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        ApiResponse<Object> apiResponse = ApiResponse.error(
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            properties.getRejectMessage()
        );
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(apiResponse));
            writer.flush();
        }
        
        if (properties.isLogEnabled()) {
            logger.info("Request rejected: {} {}", request.getMethod(), request.getRequestURI());
        }
    }
}