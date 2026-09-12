---
id: persistent-segments
sidebar_position: 16
title: SHOW PERSISTENT SEGMENTS
---

:::info[Note]
SDK method: `getPersistentSegmentInfo`.
:::

```sql
SHOW PERSISTENT SEGMENTS FROM TABLE books;
```

| Statement | Returned columns |
| --- | --- |
| SHOW PERSISTENT SEGMENTS | SEGMENT_ID, COLLECTION_ID, PARTITION_ID: BIGINT; COLLECTION_NAME: VARCHAR; NUM_ROWS: BIGINT; STATE, LEVEL: VARCHAR; STORAGE_VERSION: BIGINT; IS_SORTED: BOOLEAN |

Use executeQuery() or execute() to read the ResultSet. This command reads server state without loading collections, flushing data or scanning entities.

Each segment occupies one row. NUM_ROWS is a physical segment statistic, not a business COUNT; MEM_SIZE, when present, is in bytes. NODE_IDS is JSON array text. Missing values remain as returned by the SDK. Snapshots from different nodes are not transactionally consistent.
