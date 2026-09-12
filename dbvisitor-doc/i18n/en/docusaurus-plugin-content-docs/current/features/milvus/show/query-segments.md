---
id: query-segments
sidebar_position: 17
title: SHOW QUERY SEGMENTS
---

:::info[Note]
SDK method: `getQuerySegmentInfo`.
:::

```sql
SHOW QUERY SEGMENTS FROM TABLE books;
```

| Statement | Returned columns |
| --- | --- |
| SHOW QUERY SEGMENTS | SEGMENT_ID, COLLECTION_ID, PARTITION_ID, MEM_SIZE, NUM_ROWS: BIGINT; INDEX_NAME: VARCHAR; INDEX_ID: BIGINT; STATE, LEVEL, NODE_IDS: VARCHAR (NODE_IDS contains JSON array text); STORAGE_VERSION: BIGINT; IS_SORTED: BOOLEAN |

Use executeQuery() or execute() to read the ResultSet. This command reads server state without loading collections, flushing data or scanning entities.

Each segment occupies one row. NUM_ROWS is a physical segment statistic, not a business COUNT; MEM_SIZE, when present, is in bytes. NODE_IDS is JSON array text. Missing values remain as returned by the SDK. Snapshots from different nodes are not transactionally consistent.
