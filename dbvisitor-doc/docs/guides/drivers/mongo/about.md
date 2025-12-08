---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: 简介
description: jdbc-mongo 是一个 MongoDB 的 JDBC 驱动适配器，它允许开发者使用标准的 JDBC 接口和 MongoDB 命令的方式来操作数据。
---

jdbc-mongo 是一个 MongoDB 的 JDBC 驱动适配器，它允许开发者使用标准的 JDBC 接口和 MongoDB 命令的方式来操作数据。
目的是通过熟悉 JDBC 编程模型，使开发者能够无缝地使用 MongoDB。

## 核心特性

- JDBC 兼容，实现了标准的 JDBC 接口，可以无缝集成到任何支持 JDBC 的框架中。
- 丰富的命令集，支持 MongoDB 的常用命令，包括 [集合](./commands#collection)、[数据库管理](./commands#database)、[索引管理](./commands#index)、[用户管理](./commands#user) 等。
- 支持 命令参数占位符 “?”，并使用 `PreparedStatement` 设置参数。
- 支持 多命令执行并通过 JDBC 标准方法获取多命令执行结果。
- 支持 `Statement` 的 `maxRows`、`fetchSize`、`timeoutSec` 属性设置。
- 支持 指令拦截器，可用于日志记录、性能监控等场景。
- 支持 类型转换，例如 结果集返回为 `LONG` 类型时，可通过 `ResultSet.getInt` 或 `ResultSet.getString` 获取数据。
- 支持 `BLOB`、`CLOB`、`NCLOB` 方式读取。

## 架构设计

jdbc-mongo 内部使用 MongoDB 官方驱动进行通信，通过 ANTLR4 解析命令，并将其转换为 MongoDB 的 BSON 操作。

## 适用场景

- 需要在 Java 项目中以统一的方式（JDBC）访问 MongoDB。
- 希望使用原始命令语法操作 MongoDB。
- 需要将 MongoDB 集成到现有的基于 JDBC 的数据处理流程中。
