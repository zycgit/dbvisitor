---
id: about
sidebar_position: 1
title: 简介
description: jdbc-mongo 的 JDBC 能力、架构、依赖与文档入口。
---

## 介绍
jdbc-mongo 是 MongoDB 的 JDBC 驱动适配器，允许开发者使用标准 JDBC 接口和原生命令风格命令操作 MongoDB。

核心价值：
- 使用标准 JDBC API（Connection、Statement、PreparedStatement、ResultSet）。
- 使用原生命令风格的命令文本映射到 MongoDB 操作。
- 通过 dbVisitor 为异构数据源提供统一的编码风格。

## 特性
- 实现 JDBC 核心接口并支持 `PreparedStatement` 占位符。
- 支持原生命令风格 Mongo 命令，支持多条命令以分号顺序执行。
- 覆盖集合、索引、用户、数据库管理类操作。
- `find` 支持链式调用 `limit(...)`、`skip(...)`、`sort(...)`、`hint(...)`。
- 结果映射：`find` 返回 `_ID` 与 `_JSON` 列；预读模式下会把文档字段展开为列。
- 预读模式可通过阈值、最大文件大小、缓存目录进行配置。

## JDBC 与内部实现

ANTLR4 解析命令，再通过官方客户端执行；公共 JDBC 层负责 Connection、Statement、PreparedStatement、ResultSet 与类型转换。支持多命令和标准 JDBC 多结果访问，但不意味着支持任意 ORM SQL、事务、JDBC batch 或完整 DatabaseMetaData。ResultSet 只读、向前遍历；可按兼容类型使用 getInt/getString 或 BLOB/CLOB/NCLOB 读取。INSERT 可通过 getGeneratedKeys 获取适配器返回的 `_id`，不能据此推断所有通用 Mapper 回填方式都适用。

## 兼容性
- JDK 17+
- MongoDB Java Driver：`mongodb-driver-sync` 5.6.1（服务端兼容性需结合具体部署验证）

## 文档导航

- [安装与使用](./usecase.mdx)：依赖、JDBC 连接、参数化读写和多结果访问。
- [连接参数](./params.md)：认证、超时、自定义客户端和预读。
- [命令参考](./commands.md)：命令覆盖、Hint 和限制。
- [dbVisitor API](../../features/mongo/usage.mdx)：JdbcTemplate、Mapper 与构造器用法。
