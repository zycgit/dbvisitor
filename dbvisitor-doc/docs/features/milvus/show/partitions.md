---
id: partitions
sidebar_position: 5
title: SHOW PARTITIONS / PARTITION
---

:::info[说明]
对应 SDK 方法：`listPartitions`。
:::

```text
SHOW PARTITIONS FROM table_name;    -- 列出某表的所有分区
SHOW PARTITION p_name ON TABLE t_name;
```

返回 PARTITION（VARCHAR）分区名称，不是实体统计数。
