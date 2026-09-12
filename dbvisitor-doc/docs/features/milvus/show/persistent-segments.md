---
id: persistent-segments
sidebar_position: 16
title: SHOW PERSISTENT SEGMENTS
---

:::info[说明]
对应 SDK 方法：`getPersistentSegmentInfo`。
:::

```sql
SHOW PERSISTENT SEGMENTS FROM TABLE books;
```

| 语句 | 返回列 |
| --- | --- |
| SHOW PERSISTENT SEGMENTS | SEGMENT_ID、COLLECTION_ID、PARTITION_ID（BIGINT）、COLLECTION_NAME（VARCHAR）、NUM_ROWS（BIGINT）、STATE、LEVEL（VARCHAR）、STORAGE_VERSION（BIGINT）、IS_SORTED（BOOLEAN） |

使用 `executeQuery()` 或 `execute()` 读取结果集。此命令只读取服务端状态，不加载集合、刷新数据或扫描实体。

每个 segment 返回一行。NUM_ROWS 是物理段统计，不能作为业务 COUNT；MEM_SIZE（如有）以字节计，NODE_IDS 为 JSON 数组文本。缺失值按 SDK 返回保留，不同节点的快照不保证事务一致。
