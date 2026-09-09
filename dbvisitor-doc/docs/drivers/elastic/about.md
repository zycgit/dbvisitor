---
id: about
sidebar_position: 1
title: 简介
description: jdbc-elastic 的 JDBC 能力、架构、依赖与文档入口。
---

## 介绍
jdbc-elastic 是 Elasticsearch 的 JDBC 驱动适配器，允许开发者使用标准 JDBC 接口和原生 REST 风格命令操作 Elasticsearch。

核心价值：
- 使用标准 JDBC API（Connection、Statement、PreparedStatement、ResultSet）。
- 使用原生 REST 风格命令映射到 Elasticsearch 操作。
- 通过 dbVisitor 为异构数据源提供统一的编码风格。

## 特性
- 实现 JDBC 核心接口并支持 `PreparedStatement` 占位符。
- 支持 REST 风格命令，支持多条命令以分号顺序执行。
- 支持搜索/统计/批量查询、文档 CRUD、索引管理与 `_cat` 查询。
- 支持 `HEAD` 请求并返回 `STATUS` 列。
- 结果映射：搜索类响应映射为 `_ID` 与 `_DOC` 列；预读模式下会展开字段列。
- 预读模式支持阈值、最大文件大小、缓存目录配置。
- 可选 `indexRefresh` 在写入时追加 `refresh=true`。
- dbVisitor 提供 Elastic6/Elastic7 方言与 realdb 场景化测试（`Elastic6Dialect`、`Elastic7Dialect`、`realdb/elastic6|elastic7`）。

## JDBC 与内部实现

ANTLR4 解析命令，再通过官方客户端执行；公共 JDBC 层负责 Connection、Statement、PreparedStatement、ResultSet 与类型转换。支持多命令和标准 JDBC 多结果访问，但不意味着支持任意 ORM SQL、事务、JDBC batch 或完整 DatabaseMetaData。ResultSet 只读、向前遍历；可按兼容类型使用 getInt/getString 或 BLOB/CLOB/NCLOB 读取。INSERT 可通过 getGeneratedKeys 获取适配器返回的 `_id`，不能据此推断所有通用 Mapper 回填方式都适用。

## 兼容性
- JDK 17+
- Elasticsearch REST Client：`elasticsearch-rest-client` 7.17.10
- Jackson：`jackson-databind` 2.18.0
- dbVisitor 含 Elastic6/Elastic7 方言与 ES6/ES7 realdb 场景化测试。

## 文档导航

- [安装与使用](./usecase.mdx)：依赖、JDBC 连接、参数化读写和多结果访问。
- [连接参数](./params.md)：认证、超时、自定义客户端和预读。
- [命令参考](./commands.md)：命令覆盖、Hint 和限制。
- [向量查询指南](./vectors.mdx)：Mapping、Lambda、原生 DSL、参数绑定和调优。
- [dbVisitor API](../../features/elastic/usage.mdx)：JdbcTemplate、Mapper 与构造器用法。
