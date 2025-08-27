package com.luckcoder.request.control.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckcoder.request.control.controller.RequestControlController;
import com.luckcoder.request.control.interceptor.RequestControlInterceptor;
import com.luckcoder.request.control.service.RequestControlService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(RequestControlProperties.class)
@ConditionalOnProperty(prefix = "request.control", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RequestControlAutoConfiguration implements WebMvcConfigurer {
    
    private final RequestControlProperties properties;
    
    public RequestControlAutoConfiguration(RequestControlProperties properties) {
        this.properties = properties;
    }
    
    @Bean
    @ConditionalOnMissingBean
    public RequestControlService requestControlService() {
        return new RequestControlService(properties);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public RequestControlInterceptor requestControlInterceptor(
            RequestControlService service,
            ObjectMapper objectMapper) {
        return new RequestControlInterceptor(service, properties, objectMapper);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public RequestControlController requestControlController(RequestControlService service) {
        return new RequestControlController(service, properties);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestControlInterceptor(requestControlService(), objectMapper()))
                .addPathPatterns("/**")
                .order(Ordered.HIGHEST_PRECEDENCE);
    }
}