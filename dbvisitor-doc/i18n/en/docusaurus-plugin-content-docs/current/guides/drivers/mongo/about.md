---
id: about
sidebar_position: 1
title: 简介
description: jdbc-mongo 是 dbVisitor 提供的 MongoDB 驱动适配器，它允许开发者使用标准的 JDBC 接口和 SQL 风格的命令来操作 MongoDB 数据。
---

`jdbc-mongo` 是 dbVisitor 提供的 MongoDB 驱动适配器，它允许开发者使用标准的 JDBC 接口和 SQL 风格的命令来操作 MongoDB 数据。

## 核心特性

- **JDBC 兼容**：实现了标准的 JDBC 接口，可以无缝集成到任何支持 JDBC 的框架中。
- **SQL 风格命令**：支持类似 SQL 的命令语法，降低学习成本。
- **丰富的命令集**：支持 MongoDB 的常用命令，包括 CRUD、聚合、索引管理等。
- **连接池支持**：可以配合 Druid, HikariCP 等连接池使用。

## 架构设计

`jdbc-mongo` 内部使用 MongoDB 官方驱动进行通信，通过 ANTLR4 解析 SQL 风格的命令，并将其转换为 MongoDB 的 BSON 操作。

## 适用场景

- 需要在 Java 项目中以统一的方式（JDBC）访问 MongoDB。
- 希望使用 SQL 风格的语法操作 MongoDB。
- 需要将 MongoDB 集成到现有的基于 JDBC 的数据处理流程中。
