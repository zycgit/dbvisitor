---
id: about
sidebar_position: 1
title: 介绍
description: Elasticsearch JDBC 驱动的接入、连接和使用。
---

`jdbc-elastic` 是可独立使用的 JDBC 驱动，使用 REST 风格命令访问 Elasticsearch，支持文档读写、搜索和索引管理。可以直接使用 `Connection`、`PreparedStatement` 和 `ResultSet`，不要求使用 dbVisitor API。

## 主要能力

- 通过 PreparedStatement 绑定请求参数，使用 ResultSet 读取搜索结果。
- 支持文档读写、索引管理、搜索、统计和 `_cat` 查询。
- 支持结果字段展开，以及预读内存阈值和缓存目录配置。

## 开始使用

1. [引入依赖](dependencies.mdx)：Maven 或 Gradle 配置。
2. [建立连接](connection.mdx)：JDBC URL、认证及连接示例。
3. [参数配置](params.md)：参数名称、默认值和单位。
4. [使用限制](limitations.md)：JDBC 接口及数据源特有限制。

## 使用前须知

- 运行环境要求 Java 17 或更高版本。
- 命令必须使用驱动支持的语法，不会自动转换任意关系型 SQL。
- 不支持 JDBC Batch 和事务。使用连接池、ORM 或其他 JDBC 工具前，请核对[驱动适配器限制](../limited.md)。

配合 dbVisitor API 使用时，可使用 Elastic6、Elastic7 方言。

[命令参考](../../features/elastic/syntax/index.md) · [dbVisitor API 用法](../../features/elastic/usage.mdx)

[向量查询](../../features/elastic/vectors.mdx)
