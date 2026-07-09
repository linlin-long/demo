# AuthDemo - 登录注册服务demo

基于 Spring Boot 3 + JWT + 阿里云短信的手机号注册/登录服务。

## 技术栈

- **Spring Boot 3.2.5** (Java 17)
- **Spring Security** — 认证授权框架
- **MyBatis-Plus** — ORM 持久层
- **JWT (jjwt 0.12.5)** — 无状态 Token 认证
- **Redis** — 短信验证码存储
- **阿里云短信服务** — 验证码发送
- **MySQL** — 用户数据存储
- **Hutool** — 工具库

## 快速开始

### 1. 环境准备

| 依赖 | 版本要求 |
|------|----------|
| JDK | 17+ |
| Maven | 3.8+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |

### 2. 初始化数据库

```sql
source sql/init.sql
```

### 3. 配置参数

编辑 `src/main/resources/application.yml`，修改以下配置：

#### 数据库连接
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auth_demo
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

#### JWT 密钥（必须修改！）
```yaml
jwt:
  # 生成方式：echo -n 'your-256-bit-secret' | base64
  secret: 你的Base64编码密钥（至少32字节）
```

#### 阿里云短信（可选，不配置则验证码打印到日志）
```yaml
aliyun:
  sms:
    access-key-id: LTAI5t...
    access-key-secret: yourSecret
    sign-name: 你的签名
    template-code: SMS_123456789
```

### 4. 启动

```bash
mvn spring-boot:run
```

或直接运行 `AuthApplication.java`。

## API 文档

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录/Token过期 |
| 1001 | 手机号已注册 |
| 1002 | 手机号未注册 |
| 1005 | 密码错误 |
| 1101 | 短信发送失败 |
| 1102 | 验证码错误 |
| 1104 | 发送过于频繁 |
| 1105 | 验证码不匹配 |

### 1. 发送验证码

```
POST /api/auth/send-sms
Content-Type: application/json

{
  "phone": "13800138000"
}
```

> **注意**：60 秒内不能重复发送

### 2. 注册

```
POST /api/auth/register
Content-Type: application/json

{
  "phone": "13800138000",
  "password": "abc123456",
  "smsCode": "123456",
  "nickname": "可选昵称"
}
```

**密码规则**：8-20位，必须包含字母和数字。

### 3. 登录

```
POST /api/auth/login
Content-Type: application/json

{
  "phone": "13800138000",
  "password": "abc123456"
}
```

### 4. 刷新 Token

```
POST /api/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 5. 获取当前用户信息

```
GET /api/auth/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## 认证方式

所有需要登录的接口，在 HTTP Header 中携带：

```
Authorization: Bearer <accessToken>
```

- **AccessToken** — 有效期 24 小时
- **RefreshToken** — 有效期 7 天，过期后需重新登录

## 项目结构

```
src/main/java/com/demo/auth/
├── AuthApplication.java         # 启动类
├── config/
│   ├── JwtProperties.java       # JWT 配置属性
│   ├── MyMetaObjectHandler.java # MyBatis-Plus 自动填充
│   ├── RedisConfig.java         # Redis 配置
│   ├── SecurityConfig.java      # Spring Security 配置
│   └── SmsProperties.java       # 短信配置属性
├── controller/
│   └── AuthController.java      # 认证接口
├── service/
│   └── AuthService.java         # 认证业务逻辑
├── mapper/
│   └── UserMapper.java          # 用户 Mapper
├── model/
│   ├── entity/User.java         # 用户实体
│   ├── dto/
│   │   ├── SendSmsRequest.java  # 发送短信请求
│   │   ├── RegisterRequest.java # 注册请求
│   │   ├── LoginRequest.java    # 登录请求
│   │   └── RefreshTokenRequest.java
│   └── vo/LoginVO.java          # 登录/注册响应
├── security/
│   ├── JwtUtil.java             # JWT 工具类
│   ├── JwtAuthenticationFilter.java  # JWT 过滤器
│   ├── LoginUser.java           # 登录用户信息
│   └── SecurityUtil.java        # 安全上下文工具
├── sms/
│   └── SmsService.java          # 短信服务
└── common/
    ├── ApiResponse.java         # 统一响应体
    ├── GlobalExceptionHandler.java  # 全局异常处理
    ├── enums/ErrorCode.java     # 错误码枚举
    └── exception/BusinessException.java  # 业务异常
```

## 说明：
### 1. @TableLogic注解：表示该字段是逻辑删除字段，默认为0，表示未删除，1表示已删除。
### 2. @TableField 注解详解
`@TableField`：表示该字段映射数据库字段，默认生效。

### 注解参数说明
| 参数 | 功能描述 | 默认值 |
| ---- | -------- | ------ |
| value | 指定实体属性对应的数据库列名 | - |
| exist | 标识是否为数据库真实字段；值为 false 时所有数据库操作都会忽略该字段 | true |
| select | 标识查询时是否返回该字段；值为 false 时查询语句不会查询该列 | true |
| fill | 字段自动填充策略，配合 MetaObjectHandler 使用 | none |
| condition | 条件查询时默认使用的匹配运算符 | `=` |
| updateStrategy | 字段更新策略，控制更新时空值处理逻辑 | none |
| jdbcType | 指定字段对应的 JDBC 数据类型 | null |

### 重点参数补充
1. **exist = false**
   多用于实体临时扩展字段（计算属性、关联临时字段等），MyBatis-Plus 生成 SQL 时会直接忽略此字段，不会出现在新增、修改、条件语句中。

2. **select = false**
   常用于密码、大文本等敏感/大容量字段，普通分页、列表查询不加载该字段，需要时自定义 SQL 查询获取。

3. **fill 可选填充枚举**
- FieldFill.DEFAULT：不自动填充
- FieldFill.INSERT：仅新增时自动填充
- FieldFill.UPDATE：仅更新时自动填充
- FieldFill.INSERT_UPDATE：新增、更新均自动填充

### 3. 在mybatis-plus中默认开启驼峰转下划线也可以手动开启
在yml文件中添加：

```
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
```
### 4. 实体类说明
| 缩写 | 全称含义 | 用途说明 |
| ---- | -------- | -------- |
| PO | 持久化对象 | 数据库实体类，与数据库表一一对应 |
| DO | 数据库操作对象 | 数据库实体类，用于和数据库交互操作 |
| VO | 视图对象 | 返回给前端展示的数据对象 |
| DTO | 数据传输对象 | 接收前端传递请求参数的对象 |
| BO | 业务对象 | 封装业务逻辑、承载业务数据的对象 |
- **为什么要区别这么多对象？**
1. 遵循单一职责，解耦各层，
- PO 只跟数据库表结构绑定，不能因为前端要少个字段就去改表结构。
- DTO 只描述接口“入参”，不能让数据库字段名直接暴露给外部调用方。
- VO 只负责“出参”视图，可以组合多个 PO 的数据，甚至字段名都与 PO 不同
- 举个例子：在登录接口中，前端只需要手机号和密码，但是后端需要返回用户信息，比如登录时间，显然单纯的数据库类中不存在这些属性
所以我们创建了 DTO 和 VO 类，将前端传递的参数映射到DTO中发送给后端，后端处理后拼接出 VO 返回给前端
2.安全性，避免数据暴露
- PO中存放的是数据库字段，比如密码，不应该直接暴露给前端，所以创建了DTO类，将前端传递的参数映射到DTO中发送给后端，后端处理后拼接出VO返回给前端 
### 5. 