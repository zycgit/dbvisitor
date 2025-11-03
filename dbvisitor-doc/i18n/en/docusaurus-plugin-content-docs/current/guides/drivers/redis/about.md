---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: 11.1 JDBC Redis 适配器
description: jdbc-redis 是一个 Redis 的 JDBC 驱动适配器，它允许开发者使用标准的 JDBC 接口和命令的方式来操作 Redis 数据。
---

jdbc-redis 是一个 Redis 的 JDBC 驱动适配器，它允许开发者使用标准的 JDBC 接口和命令的方式来操作 Redis 数据。
目的是通过熟悉 JDBC 编程模型，使开发者能够无缝地使用 Redis。

## 特性：

- 支持 **140+** 常用命令，涵盖 [DB](./commands#server)、[Server](./commands#server)、[Keys](./commands#keys)、[List](./commands#list)、[Set](./commands#set)、[StoreSet](./commands#storeset)、[String](./commands#string)、[Hash](./commands#hash) 命令集。
- 支持 JDBC 标准接口，包括 `Connection`、`Statement`、`PreparedStatement`、`ResultSet` 等。
- 支持 命令参数占位符 “?”，并使用 `PreparedStatement` 设置参数。
- 支持 多命令执行并通过 JDBC 标准方法获取多命令执行结果。
- 支持 `Statement` 的 `maxRows`、`fetchSize`、`timeoutSec` 属性设置。
- 支持 指令拦截器，可用于日志记录、性能监控等场景。
- 支持 类型转换，例如 结果集返回为 `LONG` 类型时，可通过 `ResultSet.getInt` 或 `ResultSet.getString` 获取数据。
- 支持 `BLOB`、`CLOB`、`NCLOB` 方式读取。

## 技术实现

jdbc-redis 架构设计采用了适配器模式，将标准的 JDBC 接口适配到 Redis 命令体系。主要组件包括：
- JedisConn：实现了 JDBC Connection 接口，是整个适配器的核心，负责连接管理和命令执行。
- JedisCmd：封装了 Jedis 客户端的各种命令接口，支持单实例和集群模式。
- JedisRequest：表示一个 Redis 命令请求。
- ANTLR4 解析器：用于解析 Redis 命令，生成 Redis 命令执行计划。

## 执行流程

- 用户通过 JDBC API 创建 Connection、Statement 并执行 Redis 命令。
- JedisConn 接收 Redis 命令，使用 ANTLR4 解析器解析命令。
- 解析后的命令通过 JedisCmd 转发给底层的 Jedis 客户端执行。
- 执行结果通过标准的 ResultSet 或更新计数返回给用户。

## 依赖技术
- Jedis：Redis 官方推荐的 Java 客户端，版本 6.1.0。
- ANTLR4：强大的语法解析器生成工具，用于解析 Redis 命令。
- dbVisitor-driver：基础的数据库驱动框架。
