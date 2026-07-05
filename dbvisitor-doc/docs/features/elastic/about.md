---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: ElasticSearch 特性
description: ElasticSearch 数据源使用 dbVisitor 的能力范围、API 支持和使用方式。
---

# ElasticSearch 数据源特性

dbVisitor 通过 [JDBC-Elastic](../../drivers/elastic/about) 驱动，基于 JDBC 协议访问 ElasticSearch 数据源。

## 快速了解差异

| 关注点 | ElasticSearch 行为 |
|--------|-------------------|
| API 支持 | JdbcTemplate、构造器 API、BaseMapper、注解、Mapper File |
| 主键生成 | `_id` 支持 `useGeneratedKeys` 回填 |
| 分页 | 通过 Page 对象分页（from/size） |
| 批量写入 | 不支持 executeBatch |
| 存储过程 | 不支持 |
| 向量搜索 | Elastic 7（script_score）/ Elastic 8（原生 kNN）|

**不支持：** executeBatch、存储过程

## 概念类比

不同 ElasticSearch 命令的执行结果分为三种：
- **更新数** — 类比 INSERT/UPDATE/DELETE，用 `executeUpdate` 获取
- **单行/多行结果** — 类比 SELECT 结果集，第一列为 `_ID`，第二列为 `_DOC`（均为字符串）

## 详细用法

完整的 JdbcTemplate、构造器 API、BaseMapper、注解、Mapper File 用法请阅读 [ElasticSearch 使用指南](./usage)。

## 核心话题

- [DSL 查询](./usage#exec-command)：`POST /index/_search { "query": ... }` 风格
- [_id 回填](./usage#id-fill)：`useGeneratedKeys` 回填文档 `_id`
- [分页查询](./usage#pagination)：Page 对象 + from/size

## 与通用文档的关系

通用 API 用法见 [核心API](../../guides/overview)。ES 命令语法见 [驱动适配器命令列表](../../drivers/elastic/commands)。
