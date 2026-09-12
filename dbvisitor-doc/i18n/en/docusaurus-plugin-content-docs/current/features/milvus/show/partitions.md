---
id: partitions
sidebar_position: 5
title: SHOW PARTITIONS / PARTITION
---

:::info[Note]
SDK methods: `listPartitions`.
:::

```text
SHOW PARTITIONS FROM table_name;    -- List all partitions of a table
SHOW PARTITION p_name ON TABLE t_name;
```

Returns PARTITION (VARCHAR); this is a partition name, not an entity count.
