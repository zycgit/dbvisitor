---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: 驱动适配器
description: 在 dbVisitor 中通常基于对象映射查询数据，但在一些特殊需求情况下可以通过实现特定接口来决定数据如何获取。
---

dbVisitor 适配器是从 6.1.0 版本开始引入的一个重要组建，它提供了一个灵活的 JDBC 驱动适配器实现。旨在抽象 JDBC 驱动的实现细节，使非关系型数据库能够通过请求/响应模型快速集成到基于 JDBC 的应用程序中。
作为一个通用的 JDBC 驱动适配器，主要解决了以下核心问题：
- 简化驱动实现：通过抽象 JDBC 接口的复杂性，使开发者能够更轻松地为各种数据源实现 JDBC 兼容层。
- 非关系型数据库集成：允许非关系型数据库（NoSQL）以标准化的 JDBC 接口形式被访问。
- 与 dbVisitor 框架集成：通过标准化 JDBC 接口与 dbVisitor 框架协同工作，简化数据库访问操作。

## 使用指引

- 已提供的适配器
  - **[jdbc-redis](./redis/about)** 是 Redis 的 JDBC 驱动适配器，它允许开发者使用标准的 JDBC 接口和命令的方式来操作 Redis。
- 开发新的适配器
  - **[自定义适配器](./dev/about)** 学习如何开发一个自定义的 JDBC 驱动适配器，为自己的数据库实现 JDBC 兼容层。
