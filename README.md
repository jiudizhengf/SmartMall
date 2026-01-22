# SmartMall - 智慧电商平台 (Backend)

**SmartMall** 是一个基于 **Spring Boot 3** 构建的现代化 B2C 电商平台后端系统。它不仅具备完整的电商业务闭环（商品、订单、购物车、支付），还创新性地引入了 **AI 语义检索** 和 **高并发秒杀架构**，致力于打造“更懂用户”的智慧商城。

---

## 核心亮点

*  **高并发秒杀架构**：设计了 **Redis Lua 原子预扣减** + **RabbitMQ 异步削峰** + **数据库乐观锁** 的三级缓冲体系，在内存层拦截绝大多数流量，保护数据库不被击穿。
*  **AI 智能语义搜索**：集成 **LangChain4j** 与 **PostgreSQL (pgvector)**，支持自然语言搜索（如“适合送女友的礼物”），基于向量相似度推荐商品，彻底告别传统 SQL `LIKE` 模糊匹配。
*  **多级缓存体系**：全面集成 **Spring Cache + Redis**，针对商品详情、用户信息等高频读数据进行缓存，支持自定义过期时间，大幅提升读取性能。
*  **复杂商品结构**：利用 PostgreSQL 的 **JSONB** 特性存储 SKU 动态规格属性（如颜色、内存组合），兼顾了 NoSQL 的灵活性与关系型数据库的事务性。
*  **安全认证**：基于 **Spring Security + JWT** 实现无状态认证与鉴权，提供细粒度的接口访问控制。

---

## 技术架构

系统采用典型的分层架构，核心技术选型如下：

| 模块 | 技术选型 | 说明 |
| --- | --- | --- |
| **核心框架** | Spring Boot 3.3.6 | 最新版 Spring 技术栈 |
| **ORM 框架** | MyBatis-Plus 3.5.7 | 高效的 CRUD 操作 |
| **数据库** | PostgreSQL 14+ | 核心存储，使用 JSONB 存规格，pgvector 存向量 |
| **缓存/锁** | Redis + Redisson | 缓存加速与分布式锁（解决超卖问题） |
| **消息队列** | RabbitMQ | 异步解耦、流量削峰、延迟队列（订单超时取消） |
| **AI 引擎** | LangChain4j + OpenAI | 大模型接入与 Embedding 向量化 |
| **对象存储** | Aliyun OSS | 图片等静态资源存储 |
| **接口文档** | Knife4j (Swagger 3) | 在线 API 调试文档 |

### 核心数据流说明

1. **秒杀链路**：用户请求 -> Nginx/网关 -> Redis (Lua脚本扣减库存) -> RabbitMQ (异步下单消息) -> 消费者 (写入 PostgreSQL)。
2. **搜索链路**：商品上架/更新 -> 发送 MQ 消息 -> 消费者调用 Embedding 模型向量化 -> 存入 pgvector -> 用户搜索时进行向量相似度匹配。
3. **订单链路**：下单 -> 写入数据库 -> 发送延迟消息 (TTL) -> 死信队列监听器 (检查支付状态) -> 超时未支付自动取消。

---

## 项目结构

```bash
smart-mall-backend
├── common            # 通用返回对象、异常定义、全局常量
├── config            # 配置类 (Redis, RabbitMQ, Security, AI, Knife4j...)
├── controller        # 控制层 (API 接口入口)
├── component         # 核心业务组件 (秒杀消费者, 延迟队列监听, 数据初始化)
├── entity            # 数据库实体 (MyBatis-Plus)
├── service           # 业务逻辑层 (接口与实现)
├── mapper            # 数据持久层 (DAO)
├── dto               # 数据传输对象 (接收前端参数)
├── vo                # 视图对象 (返回前端数据)
└── util              # 工具类 (JWT, 文件上传...)

```

---

##  快速开始

### 前置要求

* JDK 17+
* PostgreSQL 14+ (需安装 `vector` 插件)
* Redis 6+
* RabbitMQ 3.8+

### 1. 配置数据库

创建数据库 `smartmall`，并务必执行以下 SQL 开启向量支持：

```sql
CREATE EXTENSION IF NOT EXISTS vector;

```

### 2. 修改配置

编辑 `src/main/resources/application.yml`，填入您的数据库、Redis、RabbitMQ 连接信息。如果需要使用 AI 搜索，请配置 `langchain4j.open-ai.api-key`。

### 3. 启动项目

运行 `SmartMallBackendApplication.java`。

* 项目包含 `DataInitializer`，启动时会自动检测并生成测试用户、商品 SKU 和向量数据，**开箱即用**。
* 接口文档地址：`http://localhost:8080/doc.html`

---

##  功能演示

### 1. 秒杀流程

* **预热**: 管理员调用接口将数据库库存加载至 Redis。
* **抢购**: 用户发起请求，Redis 瞬间完成库存扣减与限购校验，请求被放入 MQ 慢慢消费，前端无需等待数据库响应。

### 2. AI 语义搜索

* **自动同步**: 商品上架时，系统自动将其标题、描述、规格聚合成一段文本，计算向量后存入数据库。
* **智能搜索**: 用户输入“适合送长辈的礼物”，系统自动理解语义并推荐相关商品。

---

## 📄 开源协议

MIT License. Copyright (c) 2026 SmartMall Team.
