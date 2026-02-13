---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: 驱动适配器
description: 基于 dbvisitor-driver 的 JDBC 驱动适配器，将非关系型数据库以标准 JDBC 接口形式接入。
---

JDBC 驱动适配器是一组基于 `dbvisitor-driver` 框架实现的 JDBC 驱动，它们将非关系型数据库以标准 JDBC 接口形式接入。

每个适配器都是独立的 JDBC 驱动，可以在任何支持 JDBC 的框架中使用（如 Spring JDBC、MyBatis、dbVisitor 等），也可以直接通过原生 JDBC API 使用。
主要解决了以下核心问题：
- 简化驱动实现：通过抽象 JDBC 接口的复杂性，使开发者能够更轻松地为各种数据源实现 JDBC 兼容层。
- 非关系型数据库集成：允许非关系型数据库（NoSQL）以标准化的 JDBC 接口形式被访问。
- 统一接入：通过标准化 JDBC 接口可以与任何基于 JDBC 的框架协同工作，简化数据库访问操作。

## 使用指引

- 已提供的适配器
  - **[jdbc-redis](./redis/about)** 是 Redis 的 JDBC 驱动适配器，它允许开发者使用标准的 JDBC 接口和 Redis 命令的方式来操作数据库。
  - **[jdbc-mongo](./mongo/about)** 是 MongoDB 的 JDBC 驱动适配器，它允许开发者使用标准的 JDBC 接口和 MongoDB 命令的方式来操作数据库。
  - **[jdbc-elastic](./elastic/about)** 是一个 ElasticSearch 的 JDBC 驱动适配器，它允许开发者使用标准的 JDBC 接口和 ElasticSearch 原生 QueryDSL 的命令来操作数据。
  - **[jdbc-milvus](./milvus/about)** 是一个 Milvus 向量数据库的 JDBC 驱动适配器，它允许开发者使用标准的 JDBC 接口和 SQL 风格命令来操作 Milvus。
- 开发新的适配器
  - **[自定义适配器](./dev/about)** 学习如何开发一个自定义的 JDBC 驱动适配器，为自己的数据库实现 JDBC 兼容层。
