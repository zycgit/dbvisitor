---
id: stats
sidebar_position: 4
title: SHOW STATS
---

:::info[Note]
SDK methods: `getCollectionStats`, `getPartitionStats`.
:::

Read native collection and partition statistics with:

```sql
SHOW STATS FROM table_name;
SHOW STATS FROM table_name PARTITION partition_name;
```

The result is one row containing `NUM_ENTITIES` (BIGINT) and `STATS` (VARCHAR, the original statistics map serialized as a JSON object). These expose the SDK's entity count and statistics details. The command reads server statistics without WHERE, implicit FLUSH or a client-side scan. Use COUNT for a filtered logical row count; do not interpret a statistics snapshot as a strongly consistent, transactional count. Native APIs: [collection statistics](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/getCollectionStats.md) and [partition statistics](https://milvus.io/api-reference/java/v2.6.x/v2/Partitions/getPartitionStats.md).
