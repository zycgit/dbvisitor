---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: PostgreSQL
description: PostgreSQL 在 dbVisitor 中的使用方式。
---

# PostgreSQL

PostgreSQL 可使用 JdbcTemplate、方法注解、Mapper 文件和构造器 API。下面按使用场景介绍对应的配置与用法。

| 场景 | 使用方式 |
| --- | --- |
| 主键生成 | IDENTITY、SERIAL 与序列 |
| 分页查询 | LIMIT / OFFSET |
| 插入冲突 | ON CONFLICT |
| 数据回填 | RETURNING 返回写入字段 |
| 向量数据操作 | pgvector 读写与距离查询 |

- [类型支持](./types.md)：选择 Java 属性类型。
- [分页查询](./pagination.mdx)：查询指定页及总记录数。
- [插入冲突](./conflict.mdx)：插入时处理已有记录。
- [主键生成](./generated-keys.mdx)：配置并获取主键。
- [数据回填](./backfill.mdx)：获取写入时返回的字段值。
- [向量数据操作](./vectors.mdx)：配置向量字段、距离阈值和检索索引。
- [多条写入一致性](./write.mdx)：多次写入失败时整体回滚。
