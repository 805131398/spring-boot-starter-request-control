# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

这是一个Spring Boot请求控制工具，允许通过特定API动态控制系统是否接受外部请求。项目使用Maven作为构建工具，目标Java版本为17。

### 核心功能
- 动态启用/禁用系统请求处理
- 提供安全的控制接口
- 支持密钥验证
- 系统启动时默认允许请求
- 控制接口本身不受限制影响

## Build & Development Commands

```bash
# 编译项目
mvn compile

# 运行Spring Boot应用
mvn spring-boot:run

# 打包项目
mvn package

# 清理构建产物
mvn clean

# 运行测试
mvn test

# 编译测试代码
mvn test-compile

# 直接运行jar包
java -jar target/spring-request-controller-1.0.0.jar
```

## Project Structure

- `src/main/java/com/luckcoder/` - 主要的Java源代码
  - `request/control/` - 请求控制核心模块
    - `config/` - 配置类
    - `controller/` - REST控制器
    - `service/` - 业务逻辑服务
    - `interceptor/` - 请求拦截器
    - `dto/` - 数据传输对象
- `src/main/resources/` - 资源文件
  - `META-INF/spring.factories` - Spring Boot自动配置
  - `application.yml` - 应用配置
- `src/test/java/` - 测试代码
- `pom.xml` - Maven项目配置文件

## API 接口

### 控制接口 (默认路径: /set-request)
- `GET /set-request/true/{secretKey}` - 启用请求处理
- `GET /set-request/false/{secretKey}` - 禁用请求处理
- `GET /set-request/status/{secretKey}` - 查询当前状态
- `GET /set-request/info` - 查询基本信息（无需密钥）

### 测试接口
- `GET /test` - 测试API
- `GET /hello` - Hello World接口

## Technical Details

- Java版本: 17
- 框架: Spring Boot 3.1.5
- 构建工具: Maven 
- 项目编码: UTF-8
- GroupId: com.luckcoder
- ArtifactId: spring-boot-starter-request-control
- 包名: com.luckcoder.request.control
- 默认密钥: 470727