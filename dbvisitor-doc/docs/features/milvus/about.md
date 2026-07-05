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
| 主键生成 | 不支持 `RETURN_GENERATED_KEYS`（应用侧生成主键） |
| 分页 | `LIMIT ? OFFSET ?` |
| 批量写入 | 不支持 executeBatch |
| 存储过程 | 不支持 |
| 向量搜索 | KNN 近邻搜索（L2/Cosine/IP）+ 范围搜索 |

**不支持：** executeBatch、存储过程、`Statement.RETURN_GENERATED_KEYS`

## 概念类比

Milvus 适配器使用标准 SQL 风格语法：
- **DDL** — `CREATE TABLE`、`DROP TABLE`、`CREATE INDEX`，用 `executeUpdate` 执行
- **DML** — `INSERT`、`UPDATE`、`DELETE`，用 `executeUpdate` 获取影响行数
- **DQL** — `SELECT` 查询返回标准 `ResultSet`

:::info[Milvus 特殊要求]
Milvus 要求在查询前将集合 **加载到内存**，需先执行 `LOAD TABLE table_name` 命令。Update 本质是 "Search-to-Upsert"，不建议全表 Update。
:::

## 详细用法

完整的 JdbcTemplate、构造器 API、BaseMapper、注解、Mapper File 用法请阅读 [Milvus 使用指南](./usage)。

## 核心话题

- [向量搜索](./usage#vector-search)：KNN 近邻搜索（`orderByL2`/`orderByCosine`/`orderByIP`）+ 范围搜索（`vectorByL2`/`vectorByCosine`/`vectorByIP`）
- [混合查询](./usage#hybrid-query)：标量过滤 + 向量搜索
- [一致性级别](./usage#consistency)：`consistencyLevel=Strong` 确保即时可见

## 与通用文档的关系

通用 API 用法见 [核心API](../../guides/overview)。向量查询 API 见 [向量查询](../../guides/core/vector_query/about)。
