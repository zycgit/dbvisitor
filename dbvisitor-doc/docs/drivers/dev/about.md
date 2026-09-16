---
id: about
sidebar_position: 0
title: 1. 架构设计
sidebar_label: 1. 架构设计
description: JDBC 驱动适配器的分层结构、组件职责与执行流程。
---

驱动适配器把数据库的原生命令和 SDK 结果接入标准 JDBC。应用继续使用 Connection、Statement 和 ResultSet；适配器负责理解命令，并调用目标数据库。

## 分层结构

| 层次 | 负责什么 |
| --- | --- |
| 应用访问 | 通过 JDBC、JdbcTemplate、方法注解或 Mapper 文件提交命令，读取结果 |
| 公共 JDBC 层 | 管理连接和语句状态、绑定参数、提供标准结果集接口 |
| 数据源适配器 | 解析原生命令、组织 SDK 请求、提供结果游标 |
| 官方 SDK 与数据库 | 执行查询和写入，提供数据库原生能力 |

SQL、Redis 命令和 MongoDB 命令在这里都是待执行的命令文本。公共 JDBC 层不把它们转换为同一种 SQL，也不为数据库补出事务、关联查询等能力。

构造器 API 和 BaseMapper 需要先生成命令：这由 dbVisitor 的数据库方言负责，再交给 JDBC 驱动执行。**方言负责生成命令，适配器负责执行命令**；驱动适配器也可以脱离 dbVisitor 单独使用。

## 核心组件

核心接口位于 `net.hasor.dbvisitor.driver` 包。一次连接及其请求由以下组件协作完成：

| 组件 | 职责 | 生命周期 |
| --- | --- | --- |
| `AdapterFactory` | 按适配器名称创建连接与类型支持 | 通过 SPI 注册的数据源入口 |
| `AdapterConnection` | 持有底层客户端，创建、执行和取消请求，关闭资源 | 对应一个 JDBC 连接 |
| `AdapterRequest` | 携带命令、绑定参数、超时、取数大小及生成键选项 | 对应一次执行请求 |
| `AdapterReceive` | 把结果集、更新计数、生成键或错误交给 JDBC 层 | 接收请求的执行结果 |
| `AdapterCursor` | 提供列信息和逐行数据 | 随结果集读取与关闭 |

请求对象描述“执行什么”，连接负责“如何执行”。`AdapterReceive` 负责交付结果，`AdapterCursor` 负责读取结果中的数据，两者不是同一个职责。

## 建立连接

1. JDBC 驱动从 URL 中识别适配器名称，并合并连接参数。
2. 查找已注册的 `AdapterFactory`，创建 `TypeSupport` 和 `AdapterConnection`。
3. 工厂初始化底层 SDK 客户端，交给适配器连接管理。
4. JDBC 连接识别适配器提供的事务与元信息能力，供标准 JDBC 方法调用。

应用关闭 JDBC 连接时，适配器连接负责释放底层资源。语句和请求复用该连接，不应每次执行都重新建立数据库连接。

## 命令执行与结果读取

| 阶段 | 执行动作 |
| --- | --- |
| 创建请求 | Statement 将命令交给 `newRequest`，形成 `AdapterRequest` |
| 绑定参数 | JDBC 层将参数及语句选项放入请求 |
| 执行命令 | `doRequest` 解析命令，组织并调用 SDK 请求 |
| 交付结果 | 通过 `AdapterReceive` 返回游标、更新计数或错误，并通知请求结束 |
| 读取数据 | ResultSet 从 `AdapterCursor` 取行；按目标 Java 类型读取时选择转换器 |
| 对象映射 | 使用 dbVisitor 时，再由其映射规则和 TypeHandler 组装属性或对象 |

命令解析由适配器决定。现有适配器使用 ANTLR，但公共 JDBC 层不要求每个驱动都使用它。

一次执行可以产生多个 JDBC 结果。结果游标可以缓冲小结果集，也可以按需向 SDK 取页；**请求结束不等于结果已全部读完**。结果集关闭时，游标还需要释放取数资源。

## 扩展能力

这些接口扩展已有连接或类型服务，不是另一套执行框架：

| 接口 | 职责 | 协作方式 |
| --- | --- | --- |
| `TransactionSupport` | 自动提交、隔离级别、提交和回滚 | 连接提供能力，JDBC 事务方法委托给它 |
| `TypeSupport` | 描述类型名称、JDBC 类型与 Java 类型，选择转换器 | 工厂提供类型服务，供参数识别和结果读取使用 |
| `TypeConvert` | 把一个结果值转换为目标 Java 类型 | 由 TypeSupport 选择，不单独注册到连接 |
| `MetadataSupport` | 按路径查询库、Schema、表、视图和字段 | 连接提供原生节点，JDBC 层组织为标准元信息结果集 |

### 事务边界

只有底层客户端能让多条命令共享事务时，才应提供 `TransactionSupport`。公共 JDBC 层负责转发事务操作，不模拟事务；该接口也不包含保存点能力。

### 类型与转换

`TypeSupport` 回答“这是什么类型、应使用哪个转换器”，`TypeConvert` 完成一次具体转换。它们服务于驱动层，不替代 dbVisitor 的实体字段 TypeHandler，也不自动完成 SDK 写入参数的序列化。

### 元信息边界

`MetadataSupport` 返回数据库实际存在的对象和字段。适配器负责查询原生结构，公共 JDBC 层负责名称模式过滤、排序和标准结果列。不应把 Redis key 伪装成表，也不应凭空推断不存在的字段结构。

## 开始实现

[自定义驱动](./guide)按创建模块、实现连接与请求、返回结果、注册驱动的顺序提供示例，并说明如何按需接入上述扩展能力。
