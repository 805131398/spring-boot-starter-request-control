# Spring Request Controller

一个轻量级的Spring Boot组件，允许通过特定API动态控制系统是否接受外部请求。

## 功能特性

- ✅ 动态启用/禁用系统请求处理
- ✅ 提供安全的控制接口
- ✅ 支持密钥验证
- ✅ 系统启动时默认允许请求
- ✅ 控制接口本身不受限制影响
- ✅ 可自定义白名单路径
- ✅ 支持请求日志记录

## 快速开始

### 1. 运行应用

```bash
mvn spring-boot:run
```

或者使用IDE直接运行 `Main.java`

### 2. 测试接口

**测试普通接口（会受到控制影响）:**
```bash
curl http://localhost:8080/test
curl http://localhost:8080/hello
```

**控制接口（不受限制影响）:**
```bash
# 禁用系统请求
curl http://localhost:8080/set-request/false/470727

# 启用系统请求  
curl http://localhost:8080/set-request/true/470727

# 查询状态
curl http://localhost:8080/set-request/status/470727

# 查询基本信息（无需密钥）
curl http://localhost:8080/set-request/info
```

### 3. 测试效果

1. 首次启动时，所有接口都可正常访问
2. 调用 `/set-request/false/470727` 禁用请求后，`/test` 和 `/hello` 接口将返回503错误
3. 控制接口 `/set-request/**` 始终可访问
4. 调用 `/set-request/true/470727` 可重新启用请求

## 配置说明

在 `application.yml` 中可以配置以下参数：

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

## 项目结构

```
src/main/java/com/luckcoder/
├── Main.java                                    # Spring Boot启动类
├── TestController.java                          # 测试控制器
└── request/control/                             # 请求控制核心模块
    ├── config/
    │   ├── RequestControlProperties.java        # 配置属性
    │   └── RequestControlAutoConfiguration.java # 自动配置
    ├── controller/
    │   └── RequestControlController.java        # 控制接口
    ├── service/
    │   └── RequestControlService.java           # 核心业务逻辑
    ├── interceptor/
    │   └── RequestControlInterceptor.java       # 请求拦截器
    └── dto/
        ├── ApiResponse.java                     # 通用响应类
        └── RequestStatusInfo.java               # 状态信息DTO
```

## API 接口文档

### 控制接口

| 接口 | 方法 | 说明 | 参数 |
|-----|------|------|------|
| `/set-request/{enabled}/{secretKey}` | GET | 设置请求状态 | enabled: true/false, secretKey: 密钥 |
| `/set-request/status/{secretKey}` | GET | 查询状态 | secretKey: 密钥 |
| `/set-request/info` | GET | 基本信息 | 无需密钥 |

### 响应格式

```json
{
  "success": true,
  "message": "Request processing enabled successfully",
  "data": {
    "enabled": true,
    "timestamp": "2025-08-27T02:00:00Z",
    "operator": "api"
  },
  "code": 200
}
```

## 安全考虑

1. **密钥保护**: 生产环境建议通过环境变量配置密钥
2. **HTTPS**: 生产环境建议使用HTTPS
3. **访问控制**: 可以结合Spring Security进行更精细的权限控制
4. **请求限制**: 可以添加IP白名单或请求频率限制

## 技术栈

- Java 17
- Spring Boot 3.1.5
- Maven
- Jackson

## 作者

zhanghao - 2025/08/27