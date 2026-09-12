---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: ClickHouse
description: ClickHouse 在 dbVisitor 中的使用方式。
---

# ClickHouse

可通过 JdbcTemplate、Mapper 和构造器 API 查询、插入 ClickHouse 数据。MergeTree 表的数据变更见下方专门示例。

| 场景 | 使用方式 |
| --- | --- |
| 主键生成 | 应用侧生成 ID |
| 分页查询 | LIMIT 偏移分页 |
| 插入冲突 | MergeTree 重复记录处理 |
| 数据变更 | 提交、等待与检查 mutation |

- [类型支持](./types.md)：选择 Java 属性类型。
- [分页查询](./pagination.mdx)：查询指定页及总记录数。
- [重复记录处理](./conflict.mdx)：查询重复 ID 的最新版本。
- [主键生成](./generated-keys.mdx)：配置并获取主键。
- [批量插入](./batch-insert.mdx)：统一提交多条事件。
- [等待数据变更完成](./write.mdx)：等待变更并检查失败原因。
