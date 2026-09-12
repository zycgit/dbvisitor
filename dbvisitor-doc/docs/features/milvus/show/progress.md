---
id: progress
sidebar_position: 11
title: SHOW PROGRESS
---

:::info[说明]
对应 SDK 方法：`getLoadStateV2`、`describeIndex`。
:::

## 查看加载进度

```sql
SHOW PROGRESS OF LOADING ON TABLE table_name;
SHOW PROGRESS OF LOADING ON TABLE table_name PARTITION partition_name;
```


## 查看索引构建进度

```sql
SHOW PROGRESS OF INDEX ON TABLE table_name;
SHOW PROGRESS OF INDEX index_name ON TABLE table_name;
```

加载进度读取当前加载状态，不触发 LOAD，也不表示独立刷新任务的进度。索引进度读取索引描述，返回值见 [SHOW INDEX](indexes.md#index-metadata)。

SHOW PROGRESS OF LOADING 返回 PROGRESS（BIGINT）。SHOW PROGRESS OF INDEX 返回 TOTAL、INDEXED（BIGINT）；省略索引名时累计 SDK 返回的索引记录，不是完成百分比。
