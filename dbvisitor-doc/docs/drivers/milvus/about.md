---
id: about
sidebar_position: 1
title: 简介
description: jdbc-milvus 是一个 Milvus 向量数据库的 JDBC 驱动适配器，它允许开发者使用标准的 JDBC 接口和 SQL 风格命令来操作 Milvus。
---

jdbc-milvus 是一个 Milvus 向量数据库的 JDBC 驱动适配器，它允许开发者使用标准的 JDBC 接口和 SQL 风格命令来操作 Milvus。
目的是通过熟悉 JDBC 编程模型，使开发者能够无缝地使用 Milvus 向量数据库。

## 核心特性

- JDBC 兼容，实现了标准的 JDBC 接口，可以无缝集成到任何支持 JDBC 的框架中。
- SQL 风格语法，使用 `CREATE TABLE`、`INSERT`、`SELECT`、`DELETE` 等标准 SQL 结构操作 Milvus。
- 向量搜索支持，通过 `ORDER BY field <-> vector` 表达 KNN 搜索，通过 `WHERE` 子句表达范围搜索。
- 丰富的命令集，支持 [数据库管理](./commands#database)、[表管理](./commands#table)、[索引管理](./commands#index)、[分区管理](./commands#partition)、[用户与权限管理](./commands#user) 等。详见 [语法手册](./commands)。
- 支持命令参数占位符 `?`，并使用 `PreparedStatement` 设置参数。
- 完整的 DDL 支持，包括集合创建（含向量字段定义）、索引创建（含索引类型和参数）、分区和别名管理。
- 支持 `Statement.RETURN_GENERATED_KEYS`，在执行插入操作时自动返回生成的主键。
- 支持 SQL Hint，可覆盖查询的 `LIMIT`、`OFFSET`，或将查询转换为 Count 操作。
- 支持指令拦截器，可用于日志记录、性能监控等场景。
- 向量格式灵活，支持 JSON 数组字面量、`?` 参数绑定（`List<Float>`、`float[]` 等）、批量向量搜索。

## 架构设计

jdbc-milvus 内部使用 Milvus Java SDK 进行通信，通过 ANTLR4 解析 SQL 风格命令，并将其转换为 Milvus SDK 的 API 调用。

## 适用场景

- 需要在 Java 项目中以统一的方式（JDBC）访问 Milvus 向量数据库。
- 希望使用 SQL 风格语法操作 Milvus，降低学习成本。
- 需要将 Milvus 集成到现有的基于 JDBC 的数据处理流程中。

## 兼容性

- JDK：8+
- Milvus：建议 2.3.x 或更高版本（部分特性如 `COUNT` 需要 2.2+）
