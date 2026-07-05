---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: Redis 特性
description: Redis 数据源使用 dbVisitor 的能力范围、API 支持和使用方式。
---

# Redis 数据源特性

dbVisitor 通过 [JDBC-Redis](../../drivers/redis/about) 驱动，基于 JDBC 协议访问 Redis 数据源。

## 快速了解差异

| 关注点 | Redis 行为 |
|--------|-----------|
| API 支持 | JdbcTemplate、注解、Mapper File（不支持构造器 API、BaseMapper） |
| 主键生成 | 不支持（Key-Value 模型，无主键概念） |
| 分页 | 不支持 |
| 批量写入 | 不支持 executeBatch |
| 存储过程 | 不支持 |
| 对象映射 | 不支持（通过 JSON 序列化间接支持对象读写） |

**不支持：** 构造器 API、通用 Mapper、对象映射、结果集映射、executeBatch、存储过程

## 概念类比

不同 Redis 命令的执行结果分为两种：
- **更新数** — 类比 INSERT/UPDATE/DELETE，用 `executeUpdate` 获取
- **单行/多行结果** — 类比 SELECT 结果集

## 支持的使用方式

Redis 支持三种使用方式：
- [JdbcTemplate 命令方式](./usage#exec-command)：直接执行 Redis 命令
- [注解方式](./usage#exec-annotation)：Mapper 接口 + `@Insert`/`@Query`/`@Delete`
- [文件方式](./usage#exec-file)：Mapper XML 文件

完整的用法和 Redis 数据类型操作（String、Hash、List、Set、Sorted Set）请阅读 [Redis 使用指南](./usage)。

## 核心话题

- [对象序列化](./usage#json-serialization)：`@BindTypeHandler(JsonTypeHandler.class)` 读写 Java 对象
- [数据类型操作](./usage#redis-type)：String、Hash、List、Set、Sorted Set 的 CRUD
- [批量操作](./usage#multi-key)：MGET/MSET/DEL 多个 Key

## 与通用文档的关系

通用 API 用法见 [核心API](../../guides/overview)。Redis 命令语法见 [驱动适配器命令列表](../../drivers/redis/commands)。
