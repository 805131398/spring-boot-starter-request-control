# Spring Boot Request Control Demo

这是一个演示 `spring-boot-starter-request-control` 功能的示例应用程序。

## 运行应用

```bash
# 进入demo目录
cd examples/demo-app

# 运行应用
mvn spring-boot:run
```

## 测试接口

### 1. 业务接口（受控制影响）

```bash
# 获取Hello消息
curl http://localhost:8080/api/hello

# 获取用户列表
curl http://localhost:8080/api/users

# 获取服务状态
curl http://localhost:8080/api/status
```

### 2. 健康检查接口（白名单，不受控制）

```bash
# 应用自定义健康检查
curl http://localhost:8080/health

# Spring Actuator健康检查
curl http://localhost:8080/actuator/health
```

### 3. 请求控制接口

```bash
# 动态时间密钥说明：格式为 MMHHDD（分钟小时日）
# 例如：2025-08-27 10:43:30 → 密钥为 431027
# 请根据当前时间生成密钥，格式：分钟(2位) + 小时(2位) + 日(2位)

# 禁用请求（业务接口将返回503）
# 假设当前时间是 10:43 27日，密钥就是 431027
curl http://localhost:8080/set-request/false/431027

# 启用请求
curl http://localhost:8080/set-request/true/431027

# 查询当前状态
curl http://localhost:8080/set-request/status/431027

# 获取基本信息（无需密钥）
curl http://localhost:8080/set-request/info
```

## 测试流程

1. **正常状态测试**
   ```bash
   curl http://localhost:8080/api/hello
   # 应该返回: "Hello from Spring Boot Starter Request Control Demo!"
   ```

2. **禁用请求测试**
   ```bash
   # 根据当前时间生成密钥，例如当前时间是 10:43 27日
   # 密钥格式：分钟(43) + 小时(10) + 日(27) = 431027
   
   # 禁用请求
   curl http://localhost:8080/set-request/false/431027
   
   # 测试业务接口（应该返回503）
   curl http://localhost:8080/api/hello
   # 应该返回: {"success":false,"message":"Demo service is temporarily unavailable for maintenance","data":null,"code":503}
   
   # 测试健康检查（仍然可用）
   curl http://localhost:8080/health
   # 应该返回: {"status":"UP"}
   ```

3. **重新启用请求测试**
   ```bash
   # 启用请求（密钥需要与当前时间匹配）
   curl http://localhost:8080/set-request/true/431027
   
   # 测试业务接口（恢复正常）
   curl http://localhost:8080/api/hello
   # 应该返回: "Hello from Spring Boot Starter Request Control Demo!"
   ```

## 配置说明

查看 `src/main/resources/application.yml` 中的配置：

- 使用动态时间密钥: `dynamic`（基于当前时间的MMHHDD格式）
- 启用详细日志记录
- 添加 `/health` 到白名单
- 自定义拒绝消息

## 日志输出

启用了DEBUG级别日志，可以在控制台看到详细的请求控制日志：

```
DEBUG c.l.r.c.s.RequestControlService : RequestControlService initialized with default enabled: true
INFO  c.l.r.c.s.RequestControlService  : Request status changed from true to false by secret key
INFO  c.l.r.c.i.RequestControlInterceptor : Request rejected: GET /api/hello
```