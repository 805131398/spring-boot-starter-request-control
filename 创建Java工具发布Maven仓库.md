---
title: 创建Java工具发布Maven仓库
created: 2025-08-27
source: Cherry Studio
tags: 
---
## 🤖 Assistant

我来为你设计这个Spring Boot请求控制工具的详细设计文档。

# Spring Boot Request Control Tool 设计文档

## 1. 项目概述

### 1.1 项目名称
`spring-request-controller` - Spring Boot 请求控制工具

### 1.2 项目目标
提供一个轻量级的Spring Boot组件，允许通过特定API动态控制系统是否接受外部请求，同时保护控制接口本身不受限制。

### 1.3 核心功能
- 动态启用/禁用系统请求处理
- 提供安全的控制接口
- 支持密钥验证
- 系统启动时默认允许请求
- 控制接口本身不受限制影响

## 2. 功能需求分析

### 2.1 功能需求
| 功能ID | 功能描述 | 优先级 |
|--------|----------|---------|
| F001 | 动态控制系统请求开关 | 高 |
| F002 | 密钥验证机制 | 高 |
| F003 | 控制接口白名单保护 | 高 |
| F004 | 系统启动默认状态配置 | 中 |
| F005 | 状态查询功能 | 中 |
| F006 | 请求日志记录 | 低 |

### 2.2 非功能需求
- **性能**: 拦截器处理时间 < 1ms
- **可用性**: 99.9% 可用性
- **安全性**: 密钥验证，防止未授权访问
- **扩展性**: 支持自定义配置和扩展

## 3. 系统架构设计

### 3.1 整体架构图
```
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                  │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────── │
│  │   Controller    │  │   Interceptor   │  │  Configuration │
│  │     Layer       │  │     Layer       │  │      Layer     │
│  └─────────────────┘  └─────────────────┘  └─────────────── │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────── │
│  │    Service      │  │     Storage     │  │    Security    │
│  │     Layer       │  │     Layer       │  │      Layer     │
│  └─────────────────┘  └─────────────────┘  └─────────────── │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 核心组件
1. **RequestControlInterceptor** - 请求拦截器
2. **RequestControlService** - 核心业务逻辑
3. **RequestControlController** - 控制接口
4. **RequestControlConfiguration** - 自动配置类
5. **RequestControlProperties** - 配置属性

## 4. 详细设计

### 4.1 API接口设计

#### 4.1.1 控制接口
```http
# 启用请求处理
GET /set-request/true/{secretKey}

# 禁用请求处理  
GET /set-request/false/{secretKey}

# 查询当前状态
GET /set-request/status/{secretKey}

# 无密钥状态查询（仅返回基本信息）
GET /set-request/info
```

#### 4.1.2 响应格式
```json
{
  "success": true,
  "message": "Request processing enabled successfully",
  "data": {
    "enabled": true,
    "timestamp": "2024-01-01T10:00:00Z",
    "operator": "system"
  },
  "code": 200
}
```

### 4.2 核心类设计

#### 4.2.1 配置属性类
```java
@ConfigurationProperties(prefix = "request.control")
public class RequestControlProperties {
    
    /**
     * 是否启用请求控制功能
     */
    private boolean enabled = true;
    
    /**
     * 系统启动时的默认状态
     */
    private boolean defaultEnabled = true;
    
    /**
     * 安全密钥
     */
    private String secretKey = "470727";
    
    /**
     * 控制接口路径前缀
     */
    private String controlPath = "/set-request";
    
    /**
     * 白名单路径（不受控制的路径）
     */
    private List<String> whitelistPaths = Arrays.asList(
        "/set-request/**",
        "/actuator/**",
        "/error"
    );
    
    /**
     * 是否记录拦截日志
     */
    private boolean logEnabled = false;
    
    /**
     * 拒绝请求时的响应消息
     */
    private String rejectMessage = "System is temporarily unavailable";
    
    // getters and setters...
}
```

#### 4.2.2 服务类
```java
@Service
public class RequestControlService {
    
    private final AtomicBoolean requestEnabled;
    private final RequestControlProperties properties;
    private final Logger logger = LoggerFactory.getLogger(RequestControlService.class);
    
    public RequestControlService(RequestControlProperties properties) {
        this.properties = properties;
        this.requestEnabled = new AtomicBoolean(properties.isDefaultEnabled());
    }
    
    /**
     * 设置请求状态
     */
    public boolean setRequestEnabled(boolean enabled, String secretKey) {
        if (!validateSecretKey(secretKey)) {
            logger.warn("Invalid secret key attempt: {}", secretKey);
            return false;
        }
        
        boolean oldValue = this.requestEnabled.getAndSet(enabled);
        logger.info("Request status changed from {} to {} by secret key", 
                   oldValue, enabled);
        return true;
    }
    
    /**
     * 获取当前请求状态
     */
    public boolean isRequestEnabled() {
        return requestEnabled.get();
    }
    
    /**
     * 验证密钥
     */
    private boolean validateSecretKey(String secretKey) {
        return Objects.equals(properties.getSecretKey(), secretKey);
    }
    
    /**
     * 检查路径是否在白名单中
     */
    public boolean isWhitelistPath(String path) {
        return properties.getWhitelistPaths().stream()
                .anyMatch(pattern -> new AntPathMatcher().match(pattern, path));
    }
}
```

#### 4.2.3 拦截器类
```java
@Component
public class RequestControlInterceptor implements HandlerInterceptor {
    
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
        
        // 检查功能是否启用
        if (!properties.isEnabled()) {
            return true;
        }
        
        String requestPath = request.getRequestURI();
        
        // 白名单路径直接放行
        if (requestControlService.isWhitelistPath(requestPath)) {
            return true;
        }
        
        // 检查请求是否被允许
        if (!requestControlService.isRequestEnabled()) {
            handleRejectedRequest(request, response);
            return false;
        }
        
        return true;
    }
    
    /**
     * 处理被拒绝的请求
     */
    private void handleRejectedRequest(HttpServletRequest request, 
                                     HttpServletResponse response) throws Exception {
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        ApiResponse<Object> apiResponse = ApiResponse.error(
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            properties.getRejectMessage()
        );
        
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(apiResponse));
        writer.flush();
        
        if (properties.isLogEnabled()) {
            log.info("Request rejected: {} {}", request.getMethod(), request.getRequestURI());
        }
    }
}
```

#### 4.2.4 控制器类
```java
@RestController
@RequestMapping("${request.control.control-path:/set-request}")
public class RequestControlController {
    
    private final RequestControlService requestControlService;
    
    public RequestControlController(RequestControlService requestControlService) {
        this.requestControlService = requestControlService;
    }
    
    /**
     * 设置请求状态
     */
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
    
    /**
     * 查询状态（需要密钥）
     */
    @GetMapping("/status/{secretKey}")
    public ApiResponse<RequestStatusInfo> getStatus(@PathVariable String secretKey) {
        // 验证密钥的逻辑...
        RequestStatusInfo info = new RequestStatusInfo(
            requestControlService.isRequestEnabled(),
            Instant.now(),
            "query"
        );
        return ApiResponse.success(info, "Status retrieved successfully");
    }
    
    /**
     * 基本信息查询（无需密钥）
     */
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("available", true);
        info.put("version", "1.0.0");
        info.put("timestamp", Instant.now());
        return ApiResponse.success(info, "Service information");
    }
}
```

### 4.3 自动配置类
```java
@Configuration
@EnableConfigurationProperties(RequestControlProperties.class)
@ConditionalOnProperty(prefix = "request.control", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RequestControlAutoConfiguration implements WebMvcConfigurer {
    
    @Bean
    @ConditionalOnMissingBean
    public RequestControlService requestControlService(RequestControlProperties properties) {
        return new RequestControlService(properties);
    }
    
    @Bean
    @ConditionalOnMissingBean  
    public RequestControlInterceptor requestControlInterceptor(
            RequestControlService service,
            RequestControlProperties properties,
            ObjectMapper objectMapper) {
        return new RequestControlInterceptor(service, properties, objectMapper);
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestControlInterceptor(null, null, null))
                .addPathPatterns("/**")
                .order(Ordered.HIGHEST_PRECEDENCE);
    }
}
```

## 5. 配置说明

### 5.1 application.yml配置示例
```yaml
request:
  control:
    enabled: true                    # 是否启用请求控制功能
    default-enabled: true            # 系统启动时默认状态
    secret-key: "470727"             # 安全密钥
    control-path: "/set-request"     # 控制接口路径
    log-enabled: true                # 是否记录日志
    reject-message: "系统维护中，请稍后再试"  # 拒绝请求消息
    whitelist-paths:                 # 白名单路径
      - "/set-request/**"
      - "/actuator/**"  
      - "/error"
      - "/favicon.ico"
```

## 6. 使用说明

### 6.1 Maven依赖
```xml
<dependency>
    <groupId>com.yourname</groupId>
    <artifactId>spring-request-controller</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 6.2 使用示例
```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 6.3 API调用示例
```bash
# 禁用系统请求
curl -X GET "http://localhost:8080/set-request/false/470727"

# 启用系统请求  
curl -X GET "http://localhost:8080/set-request/true/470727"

# 查询状态
curl -X GET "http://localhost:8080/set-request/status/470727"

# 查询基本信息
curl -X GET "http://localhost:8080/set-request/info"
```

## 7. 安全考虑

1. **密钥保护**: 支持环境变量配置密钥
2. **请求限制**: 可添加IP白名单或请求频率限制
3. **日志审计**: 记录所有控制操作
4. **HTTPS**: 生产环境建议使用HTTPS
5. **权限控制**: 可集成Spring Security

## 8. 扩展功能

1. **多密钥支持**: 支持不同权限级别的密钥
2. **定时任务**: 支持定时开启/关闭
3. **集群支持**: 通过Redis等实现集群状态同步
4. **监控集成**: 集成Prometheus监控
5. **管理界面**: 提供Web管理界面

这个设计提供了一个完整、安全、可扩展的请求控制解决方案。你觉得这个设计如何？有什么需要调整的地方吗？
