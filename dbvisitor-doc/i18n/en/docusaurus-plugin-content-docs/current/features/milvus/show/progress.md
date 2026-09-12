---
id: progress
sidebar_position: 11
title: SHOW PROGRESS
---

:::info[Note]
SDK methods: `getLoadStateV2`, `describeIndex`.
:::

## Check Loading Progress

```sql
SHOW PROGRESS OF LOADING ON TABLE table_name;
SHOW PROGRESS OF LOADING ON TABLE table_name PARTITION partition_name;
```


## Check Index Building Progress

```sql
SHOW PROGRESS OF INDEX ON TABLE table_name;
SHOW PROGRESS OF INDEX index_name ON TABLE table_name;
```

Loading progress observes the current load state; it does not initiate LOAD or identify an independent refresh task. Index progress reads index descriptions; see [SHOW INDEX](indexes.md#index-metadata) for returned values.

SHOW PROGRESS OF LOADING returns PROGRESS (BIGINT). SHOW PROGRESS OF INDEX returns TOTAL and INDEXED (BIGINT); without an index name it sums the index records returned by the SDK, not a percentage.
