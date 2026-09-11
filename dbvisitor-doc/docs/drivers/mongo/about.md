---
id: about
sidebar_position: 1
title: 介绍
description: MongoDB JDBC 驱动的接入、连接和使用。
---

`jdbc-mongo` 是可独立使用的 JDBC 驱动，使用 MongoDB 命令访问集合，支持文档读写、聚合和索引管理。可以直接使用 `Connection`、`PreparedStatement` 和 `ResultSet`，不要求使用 dbVisitor API。

## 主要能力

- 支持集合、文档和索引操作，以及聚合查询。
- 通过 PreparedStatement 绑定参数，查询支持分页、排序等选项。
- 支持 `_id` 主键回传和文档字段展开，可配置预读行为。

## 开始使用

1. [引入依赖](./dependencies.mdx)：Maven 或 Gradle 配置。
2. [建立连接](./connection.mdx)：JDBC URL、认证及连接示例。
3. [参数配置](./params.md)：参数名称、默认值和单位。
4. [使用限制](./limitations.md)：JDBC 接口及数据源特有限制。

## 使用前须知

- 运行环境要求 Java 17 或更高版本。
- 命令必须使用驱动支持的语法，不会自动转换任意关系型 SQL。
- 不支持 JDBC Batch 和事务。使用连接池、ORM 或其他 JDBC 工具前，请核对[驱动适配器限制](../limited.md)。

[命令参考](../../features/mongo/commands.md) · [dbVisitor API 用法](../../features/mongo/usage.mdx)
