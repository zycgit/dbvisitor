---
id: tables
sidebar_position: 2
title: SHOW TABLES / TABLE / CREATE TABLE
---

:::info[说明]
对应 SDK 方法：`listCollectionsV2`、`describeCollection`。
:::

## 查看表

```sql
SHOW TABLES;                        -- 列出所有表
SHOW TABLE table_name;              -- 查看字段信息
SHOW CREATE TABLE table_name;       -- 查看建表详细语句
```

SHOW TABLES 列出集合名称。SHOW TABLE 每个字段返回一行，包含类型、维度、主键和 AutoID 标志、可空标志、ARRAY 元素类型与容量、VARCHAR 长度、分区键和聚簇键标志。SHOW CREATE TABLE 返回 CREATE SCRIPT（VARCHAR）建表语句，不包含索引或权限。
