---
id: about
sidebar_position: 0
title: 简介
description: Redis JDBC 适配器的功能、组件和文档入口。
---

jdbc-redis 是一个 Redis 的 JDBC 驱动适配器，它允许开发者使用标准的 JDBC 接口和命令的方式来操作 Redis 数据。
目的是通过熟悉 JDBC 编程模型，使开发者能够无缝地使用 Redis。

## 核心特性

- 提供 JDBC 核心接口；框架仍须使用驱动支持的命令和调用方式。
- 支持 **140+** 常用命令，涵盖 [DB](./commands#server)、[Server](./commands#server)、[Keys](./commands#keys)、[List](./commands#list)、[Set](./commands#set)、[StoreSet](./commands#storeset)、[String](./commands#string)、[Hash](./commands#hash) 命令集。
- 支持 命令参数占位符 “?”，并使用 `PreparedStatement` 设置参数。
- 支持 多命令执行并通过 JDBC 标准方法获取多命令执行结果。
- 支持 `Statement` 的 `maxRows`、`fetchSize`、`setQueryTimeout(seconds)` 属性设置。
- 支持 指令拦截器，可用于日志记录、性能监控等场景。
- 支持 类型转换，例如 结果集返回为 `LONG` 类型时，可通过 `ResultSet.getInt` 或 `ResultSet.getString` 获取数据。
- 支持 `BLOB`、`CLOB`、`NCLOB` 方式读取。

## 技术实现

### 架构设计

jdbc-redis 项目采用了适配器模式，将标准的 JDBC 接口适配到 Redis 命令体系。主要组件包括：
- JedisConn：连接公共 JDBC 层与 Redis 适配器，负责命令解析和连接状态；对外的 JDBC Connection 由公共驱动提供。
- JedisCmd：封装了 Jedis 客户端的各种命令接口，支持单实例和集群模式。
- JedisRequest：表示一个 Redis 命令请求。
- ANTLR4 解析器：用于解析 Redis 命令，生成 Redis 命令执行计划。

### 命令执行流程

- 用户通过 JDBC API 创建 Connection、Statement 并执行 Redis 命令。
- JedisConn 接收 Redis 命令，使用 ANTLR4 解析器解析命令。
- 解析后的命令通过 JedisCmd 转发给底层的 Jedis 客户端执行。
- 执行结果通过标准的 ResultSet 或更新计数返回给用户。

### 依赖技术
- Jedis：适配器使用的 Java 客户端，版本 6.1.0。
- ANTLR4：强大的语法解析器生成工具，用于解析 Redis 命令。
- dbVisitor-driver：基础的数据库驱动框架。

## 适用场景

- 需要在 Java 项目中以统一的方式（JDBC）访问 Redis。
- 希望使用原始命令语法操作 Redis。
- 需要将 Redis 集成到现有的基于 JDBC 的数据处理流程中。

## 版本与文档

当前开发版要求 Java 17+，依赖 Jedis 6.1.0、ANTLR4 和 dbvisitor-driver。客户端依赖版本不等于服务端全部新命令的支持承诺；可用命令以本驱动语法手册为准。

[安装与使用](./usecase.mdx) · [连接参数](./params.md) · [149 条命令与返回列](./commands.md) · [dbVisitor API](../../features/redis/usage.mdx)
