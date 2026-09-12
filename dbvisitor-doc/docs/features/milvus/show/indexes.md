---
id: indexes
sidebar_position: 3
title: SHOW INDEXES / INDEX
---

:::info[说明]
对应 SDK 方法：`listIndexes`、`describeIndex`。
:::

## 查看索引 {#index-metadata}

```sql
SHOW INDEXES FROM table_name;
SHOW INDEX index_name ON TABLE table_name;
SHOW PROGRESS OF INDEX ON TABLE table_name;
SHOW PROGRESS OF INDEX index_name ON TABLE table_name;
```

索引详情返回 `INDEX`、`FIELD`、`ID`、`PARAMS` 四列；集合没有索引时返回保留列元数据的空结果集。查询全部索引时，驱动先调用 SDK `listIndexes`，再按名称调用 `describeIndex`；指定索引名时直接查询该索引。它们不是原子快照，若查询期间索引被删除或原生请求失败，会传播 SQLException，不返回已收集的部分列表当作成功。

`INDEX` 和 `FIELD` 为 VARCHAR，`ID` 为 BIGINT。`PARAMS` 为逗号分隔的 `key=value` 文本（VARCHAR），不是 JSON。

进度返回一行 BIGINT `TOTAL` 与 `INDEXED`，分别为 SDK 的总行数和已索引行数。省略索引名时按各索引累加；同一批实体有两个索引时可能被计数两次，因此不是集合实体数或 COUNT 的替代。没有索引时两者为 0，原生计数的刷新时机由 Milvus 决定。
