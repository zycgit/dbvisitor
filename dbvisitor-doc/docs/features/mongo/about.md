---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: MongoDB 特性
description: MongoDB 数据源使用 dbVisitor 的能力范围、API 支持和使用方式。
---

# MongoDB 数据源特性

dbVisitor 通过 [JDBC-Mongo](../../drivers/mongo/about) 驱动，基于 JDBC 协议访问 MongoDB 数据源。

## 快速了解差异

| 关注点 | MongoDB 行为 |
|--------|-------------|
| API 支持 | JdbcTemplate、构造器 API、BaseMapper、注解、Mapper File |
| 主键生成 | `_id` 由 MongoDB 或应用侧生成，支持 `useGeneratedKeys` 回填 |
| 分页 | 通过 Page 对象分页 |
| 批量写入 | 不支持 executeBatch |
| 存储过程 | 不支持 |
| 对象映射 | 支持（需用 `ObjectId(?)` 模板处理 `_id` 查询） |

**不支持：** JDBC addBatch/executeBatch、事务、存储过程

## 概念类比

不同 MongoDB 命令的执行结果分为两种：
- **更新数** — 类比 INSERT/UPDATE/DELETE，用 `executeUpdate` 获取
- **单行/多行结果** — 类比 SELECT 结果集，文档查询的第一列为 `_ID`，第二列为 `_JSON`（均为字符串）

## 详细用法

完整的 JdbcTemplate、构造器 API、BaseMapper、注解、Mapper File 用法请阅读 [MongoDB 使用指南](./usage)。

## 核心话题

- [ObjectId 映射](./usage#object-id)：`_id` 字段 String ↔ ObjectId 转换
- [_id 回填](./usage#id-fill)：`useGeneratedKeys` 回填插入文档的 `_id`
- [分页查询](./usage#pagination)：Page 对象 + Mapper 方法

## 与通用文档的关系

通用 API 用法见 [核心API](../../guides/overview)。MongoDB 命令语法见 [驱动适配器命令列表](../../drivers/mongo/commands)。
