---
id: overview
sidebar_position: 0
hide_table_of_contents: true
title: 数据源
description: 按具体数据源说明 dbVisitor 在类型、方言、主键回填、函数、过程和专有能力上的使用方式与差异。
---

# 数据源

选择数据源后，可以在这里了解哪些 dbVisitor API 可用、相同调用最终执行什么数据库操作，以及类型、主键、分页和写入行为的差异。数据库专有能力也按数据源介绍，并说明通过原生命令还是厂商客户端使用。

本栏目专注于某个具体数据源下的使用方式和差异，包括：

- 快速了解该数据源的行为差异（主键生成、分页、写入冲突、批量、存储过程等）
- 类型映射和推荐字段类型
- 自增主键、序列、**selectKey**、返回键等主键生成方式
- 方言特性（分页、重复键策略、函数、过程、批量写入）
- 数据源专有 SQL、DSL 或限制
- 与通用 API 文档不同的注意事项

## 关系型数据库

- [MySQL](./mysql/about)：`AUTO_INCREMENT`、JDBC generated keys、`INSERT IGNORE`/`ON DUPLICATE KEY UPDATE`、`TINYINT(1)` 布尔映射
- [PostgreSQL](./postgresql/about)：`SERIAL`、`RETURNING`、`ON CONFLICT`、pgvector 向量搜索、序列
- [Oracle](./oracle/about)：`IDENTITY`、sequence、`keyColumn` 必要性、`MERGE` 冲突策略、`RETURNING INTO` 限制
- [SQL Server](./mssql/about)：`OUTPUT INSERTED`、`MERGE`、`ROW_NUMBER()` 分页、`ORDER BY` 去重
- [DB2](./db2/about)：`IDENTITY`、sequence、batch generated keys 限制、`MERGE` 冲突策略
- [达梦](./dm/about)：自增列、`IGNORE_ROW_ON_DUPKEY_INDEX`、`MERGE`、序列
- [H2](./h2/about)：`IDENTITY`、sequence、`MERGE`、Java Alias
- [ClickHouse](./clickhouse/about)：应用侧生成 ID、JDBC batch、分析型写入模型

## 非关系型数据库

- [Redis](./redis/about)：JdbcTemplate + 注解 + Mapper File 三种方式；String/Hash/List/Set/Sorted Set 操作
- [MongoDB](./mongo/about)：JdbcTemplate + 构造器 + BaseMapper + 注解 + Mapper File 五种方式；`_id` 回填
- [ElasticSearch](./elastic/about)：JdbcTemplate + 构造器 + BaseMapper + 注解 + Mapper File；DSL 风格
- [Milvus](./milvus/about)：SQL 语法、KNN/范围/Hybrid 搜索、Partial Update、schema 函数与 Import

## 数据源支持矩阵

如需对比不同数据源在构造器 API、分页、序列、向量等功能上的支持差异，请阅读 [数据源支持矩阵](./support)。

## 阅读建议

如果你想学习 dbVisitor 的通用能力，请先阅读 [核心API](../guides/overview)。如果你关注 MongoDB、Redis、Elasticsearch、Milvus 等 JDBC 驱动本身，请阅读 [JDBC 驱动](../drivers/about)。

当某个数据库和通用文档行为不完全一致时，以本栏目中的数据源说明为准。

## 独立使用 JDBC 驱动

MongoDB、Elasticsearch、Redis 和 Milvus 可通过独立的 [JDBC 驱动](../drivers/about)接入，不要求使用 dbVisitor API。MySQL 等关系型数据库使用对应厂商的 JDBC 驱动。
