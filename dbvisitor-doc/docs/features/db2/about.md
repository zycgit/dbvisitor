---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: DB2
description: DB2 在 dbVisitor 中的使用方式。
---

# DB2

DB2 可使用 JdbcTemplate、方法注解、Mapper 文件和构造器 API。下面按使用场景介绍对应的配置与用法。

| 场景 | 使用方式 |
| --- | --- |
| 主键生成 | IDENTITY 与序列 |
| 分页查询 | 行号分页 |
| 插入冲突 | MERGE |
| 数据回填 | FINAL TABLE 返回写入字段 |

- [编程式 API](/docs/features/db2/programmatic)：查询、写入、多结果及存储过程与函数差异。
- [Mapper API](mapper.md)：方法注解、Mapper 读写及执行差异。
- [构造器 API](builder.md)：可用操作、数据源差异与对应用法。
- [分页查询](./pagination.mdx)：查询指定页及总记录数。
- [插入冲突](./conflict.mdx)：插入时处理已有记录。
- [主键生成](./generated-keys.mdx)：配置并获取主键。
- [数据回填](./backfill.mdx)：获取写入时返回的字段值。
- [类型支持](./types.md)：选择 Java 属性类型。
- [事务支持](./transactions.mdx)：多次写入失败时整体回滚。
