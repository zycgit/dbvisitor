---
id: indexes
sidebar_position: 3
title: SHOW INDEXES / INDEX
---

:::info[Note]
SDK methods: `listIndexes`, `describeIndex`.
:::

## Show Indexes {#index-metadata}

```sql
SHOW INDEXES FROM table_name;
SHOW INDEX index_name ON TABLE table_name;
SHOW PROGRESS OF INDEX ON TABLE table_name;
SHOW PROGRESS OF INDEX index_name ON TABLE table_name;
```

Index details return four columns: `INDEX`, `FIELD`, `ID`, and `PARAMS`. A collection without indexes returns an empty ResultSet with column metadata preserved. For all indexes, the driver calls SDK `listIndexes` followed by `describeIndex` for each name; a named request describes that index directly. These calls are not an atomic snapshot. If an index disappears or a native request fails, SQLException is propagated rather than returning the collected partial list as success.

`INDEX` and `FIELD` are VARCHAR; `ID` is BIGINT. `PARAMS` is comma-separated `key=value` text (VARCHAR), not JSON.

Progress returns one row containing BIGINT `TOTAL` and `INDEXED`, the SDK total-row and indexed-row counters. Omitting the index name sums the counters across indexes. The same entities may be counted twice when they have two indexes, so this is not collection cardinality or a replacement for COUNT. Both counters are 0 when there are no indexes; Milvus controls when native counters refresh.
