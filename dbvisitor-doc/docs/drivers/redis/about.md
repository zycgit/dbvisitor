---
id: about
sidebar_position: 0
title: 介绍
description: Redis JDBC 驱动的接入、连接和使用。
---

`jdbc-redis` 是可独立使用的 JDBC 驱动，使用 Redis 原生命令访问字符串、哈希、列表、集合和有序集合。可以直接使用 `Connection`、`PreparedStatement` 和 `ResultSet`，不要求使用 dbVisitor API。

## 主要能力

- 支持 String、Hash、List、Set、Sorted Set 等常用命令。
- 通过 PreparedStatement 绑定命令参数，使用结果集或更新计数读取响应。
- 支持单机和 Redis Cluster 连接，可配置认证、超时和集群连接池。

## 开始使用

1. [引入依赖](dependencies.mdx)：Maven 或 Gradle 配置。
2. [建立连接](connection.mdx)：JDBC URL、认证及连接示例。
3. [参数配置](params.md)：参数名称、默认值和单位。
4. [使用限制](limitations.md)：JDBC 接口及数据源特有限制。

## 使用前须知

- 运行环境要求 Java 17 或更高版本。
- 命令必须使用驱动支持的语法，不会自动转换任意关系型 SQL。
- 不支持 JDBC Batch 和事务。使用连接池、ORM 或其他 JDBC 工具前，请核对[驱动适配器限制](../limited.md)。
- JDBC URL 前缀为 `jdbc:dbvisitor:jedis://`，不是 `redis://`。

[命令参考](../../features/redis/about.md) · [dbVisitor API 用法](../../features/redis/dbvisitor/usage.mdx)
