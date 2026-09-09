---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: Milvus 特性
description: Milvus 向量数据库使用 dbVisitor 的能力范围、API 支持和向量搜索方式。
---

# Milvus 数据源特性

dbVisitor 通过 [JDBC-Milvus](../../drivers/milvus/about) 驱动，基于 JDBC 协议访问 Milvus 向量数据库。与 MongoDB/ElasticSearch 的原生命令风格不同，Milvus 适配器采用 **SQL 风格语法**（`CREATE TABLE`、`INSERT`、`SELECT`、`DELETE` 等），学习成本更低。

## 快速了解差异

| 关注点 | Milvus 行为 |
|--------|------------|
| API 支持 | JdbcTemplate、构造器 API、BaseMapper、注解、Mapper File |
| 主键生成 | JDBC INSERT/UPSERT 支持 `RETURN_GENERATED_KEYS`；按 SDK 返回 Int64/VarChar ID |
| 分页 | LIMIT/OFFSET、maxRows 控制返回范围，fetchSize 控制按需取页大小 |
| 多行写入 | 多行 VALUES 或 Iterable/Iterator；不支持 JDBC executeBatch |
| 存储过程 | 不支持 |
| 向量搜索 | 类型匹配的 KNN/范围搜索；Hybrid 多路融合返回一个结果集 |

**不支持：** 事务/savepoint、JDBC executeBatch、存储过程和可更新 ResultSet。API 使用仍受驱动 SQL 子集限制，不保证任意通用方法或 SQL 均可执行。

## 概念类比

Milvus 适配器使用 SQL 风格的命令子集：

- **DDL** — `CREATE TABLE`、`DROP TABLE`、`CREATE INDEX`，用 `executeUpdate` 执行
- **DML** — `INSERT`、`UPDATE`、`DELETE`，用 `executeUpdate` 获取影响行数
- **DQL** — `SELECT` 查询返回标准 `ResultSet`

:::info[Milvus 特殊要求]
查询前先建立所需索引并执行 `LOAD TABLE table_name`。UPDATE 分页选取主键，使用原生 Partial Upsert 仅提交 SET 字段；无 LIMIT 会持续处理符合条件的实体，但没有跨页事务、整体回滚或精确一次保证。
:::

## 详细用法

完整的 JdbcTemplate、构造器 API、BaseMapper、注解、Mapper File 用法请阅读 [Milvus 使用指南](./usage)。

## 核心话题

- [向量搜索](./usage#vector-search)：单向量 KNN、L2 范围构造器与 COSINE/IP 的 SQL 阈值规则。
- [标量过滤 + 向量搜索](./usage#hybrid-query)：与[原生 Hybrid 多路融合](../../drivers/milvus/commands.md#hybrid)不同。
- [一致性级别](./usage#consistency)：`consistencyLevel=Strong` 确保即时可见

## 与通用文档的关系

通用 API 用法见 [核心API](../../guides/overview)。向量查询 API 见 [向量查询](../../guides/core/vector_query/about)。

当前源码要求 Java 17+、SDK 2.6.22、Milvus 最低 2.6.2；服务端功能门槛、发布版区别及验证范围见[发布与支持矩阵](../../drivers/milvus/compatibility.md)。JDBC 主键用法见[类型绑定与主键回传](../../drivers/milvus/usecase.mdx#typed-values)。
