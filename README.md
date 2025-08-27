# Spring Boot Starter Request Control

[![Maven Central](https://img.shields.io/maven-central/v/com.luckcoder/spring-boot-starter-request-control.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.luckcoder%22%20AND%20a:%22spring-boot-starter-request-control%22)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

一个轻量级的Spring Boot Starter，允许通过特定API动态控制系统是否接受外部请求。

## 功能特性

- ✅ 动态启用/禁用系统请求处理
- ✅ 提供安全的控制接口
- ✅ 支持密钥验证
- ✅ 系统启动时默认允许请求
- ✅ 控制接口本身不受限制影响
- ✅ 可自定义白名单路径
- ✅ 支持请求日志记录
- ✅ Spring Boot自动配置

## 快速开始

### 1. 添加依赖

**Maven:**
```xml
<dependency>
    <groupId>com.luckcoder</groupId>
    <artifactId>spring-boot-starter-request-control</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**
```gradle
implementation 'com.luckcoder:spring-boot-starter-request-control:1.0.0'
```

### 2. 创建Spring Boot应用

```java
@SpringBootApplication
@RestController
public class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @GetMapping("/api/hello")
    public String hello() {
        return "Hello World!";
    }
    
    @GetMapping("/api/users")
    public List<String> getUsers() {
        return Arrays.asList("张三", "李四", "王五");
    }
}
```

### 3. 配置（可选）

在 `application.yml` 中配置：

```yaml
request:
  control:
    enabled: true                    # 是否启用请求控制功能
    default-enabled: true            # 系统启动时默认状态
    secret-key: "dynamic"            # 动态时间密钥或自定义密钥
    control-path: "/set-request"     # 控制接口路径
    log-enabled: true                # 是否记录日志
    reject-message: "系统维护中，请稍后再试"  # 拒绝请求消息
    whitelist-paths:                 # 白名单路径（这些路径不受控制）
      - "/set-request/**"
      - "/actuator/**"  
      - "/error"
      - "/favicon.ico"
      - "/health"                    # 可以添加自定义的健康检查路径
```

### 4. 使用控制接口

**启动应用后，可以使用以下接口控制请求：**

```bash
# 动态时间密钥说明：
# 格式为 MMHHDD（分钟小时日）
# 例如：2025-08-27 10:43:30 → 密钥为 431027

# 禁用系统请求（除白名单路径外的所有请求将返回503）
curl http://localhost:8080/set-request/false/431027

# 启用系统请求
curl http://localhost:8080/set-request/true/431027

# 查询当前状态
curl http://localhost:8080/set-request/status/431027

# 查询基本信息（无需密钥）
curl http://localhost:8080/set-request/info
```

### 5. 测试效果

1. **正常情况**：`/api/hello` 和 `/api/users` 可正常访问
2. **禁用请求后**：调用控制接口禁用后，业务接口将返回503错误
3. **控制接口**：`/set-request/**` 始终可访问
4. **重新启用**：调用启用接口后，所有接口恢复正常

## 配置参数说明

| 参数 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| `request.control.enabled` | boolean | true | 是否启用请求控制功能 |
| `request.control.default-enabled` | boolean | true | 系统启动时的默认状态 |
| `request.control.secret-key` | String | "dynamic" | 密钥类型：'dynamic'为动态时间密钥，其他值为静态密钥 |
| `request.control.control-path` | String | "/set-request" | 控制接口路径前缀 |
| `request.control.log-enabled` | boolean | false | 是否记录拦截日志 |
| `request.control.reject-message` | String | "System is temporarily unavailable" | 拒绝请求时的响应消息 |
| `request.control.whitelist-paths` | List | ["/set-request/**", "/actuator/**", "/error", "/favicon.ico"] | 白名单路径列表 |

## 架构组件

```
spring-boot-starter-request-control/
└── com.luckcoder.request.control/
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
| `/set-request/{enabled}/{secretKey}` | GET | 设置请求状态 | enabled: true/false, secretKey: 动态时间密钥或静态密钥 |
| `/set-request/status/{secretKey}` | GET | 查询状态 | secretKey: 动态时间密钥或静态密钥 |
| `/set-request/info` | GET | 基本信息 | 无需密钥 |

### 动态时间密钥规则

当配置 `secret-key: "dynamic"` 时，系统将使用动态时间密钥验证：

- **密钥格式**: MMHHDD（分钟+小时+日）
- **示例**: 2025-08-27 10:43:30 → 密钥为 `431027`
- **计算规则**: 
  - MM: 当前分钟数（00-59）
  - HH: 当前小时数（00-23）  
  - DD: 当前日期（01-31）
- **有效期**: 密钥在当前分钟内有效
- **向后兼容**: 可配置静态密钥替代动态验证

### 响应格式

**成功响应：**
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

**错误响应：**
```json
{
  "success": false,
  "message": "Invalid secret key",
  "data": null,
  "code": 401
}
```

**请求被拒绝时的响应（503）：**
```json
{
  "success": false,
  "message": "System is temporarily unavailable",
  "data": null,
  "code": 503
}
```

## 使用场景

- **系统维护**：在系统维护期间临时关闭服务
- **流量控制**：在高峰期临时限制访问
- **紧急处理**：快速响应突发事件，暂停服务
- **灰度发布**：配合发布流程控制流量
- **应急响应**：快速切断可能有问题的服务

## 发布到Maven中央仓库

本项目已经配置了发布到Maven中央仓库的完整流程：

### 准备工作
1. 在 [Sonatype JIRA](https://issues.sonatype.org/) 申请GroupId
2. 设置GPG密钥用于签名
3. 在 `~/.m2/settings.xml` 中配置Sonatype凭证

### 发布命令
```bash
# 清理并编译
mvn clean compile

# 发布到Sonatype仓库
mvn clean deploy -P release

# 发布源码和文档
mvn source:jar javadoc:jar
```

## 开发和贡献

```bash
# 克隆项目
git clone https://github.com/805131398/spring-boot-starter-request-control.git

# 编译项目
mvn clean compile

# 运行测试
mvn test

# 生成源码包
mvn source:jar

# 生成文档包
mvn javadoc:jar
```

## 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

## 技术栈

- Java 17+
- Spring Boot 3.1.5+
- Maven 3.6+

## 版本历史

- **1.0.0** - 首次发布
  - 基本的请求控制功能
  - Spring Boot自动配置支持
  - 完整的API接口

## 作者

**zhanghao**
- GitHub: [@805131398](https://github.com/805131398)
- Email: p15589940489@gmail.com

## 支持

如果您觉得这个项目对您有帮助，请给个⭐️！

如果您遇到问题或有功能建议，请在 [GitHub Issues](https://github.com/805131398/spring-boot-starter-request-control/issues) 中反馈。