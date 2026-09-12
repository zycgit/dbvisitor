---
id: stats
sidebar_position: 4
title: SHOW STATS
---

:::info[说明]
对应 SDK 方法：`getCollectionStats`、`getPartitionStats`。
:::

集合与分区的原生统计信息使用：

```sql
SHOW STATS FROM table_name;
SHOW STATS FROM table_name PARTITION partition_name;
```

返回一行 `NUM_ENTITIES`（BIGINT）和 `STATS`（VARCHAR，原始统计 Map 的 JSON 对象文本），分别对应 SDK 的实体数和统计明细。此命令读取服务端统计，不接受 WHERE，不隐式 FLUSH，也不执行客户端扫描。需要过滤后的逻辑行数时使用 COUNT；不要把统计快照当作强一致、事务性的计数结果。对应官方 API：[集合统计](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/getCollectionStats.md)、[分区统计](https://milvus.io/api-reference/java/v2.6.x/v2/Partitions/getPartitionStats.md)。
