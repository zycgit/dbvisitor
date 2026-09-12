---
id: create-partition
slug: /features/milvus/sql/partitions
sidebar_position: 11
title: CREATE PARTITION
---

:::info[说明]
对应 SDK 方法：`createPartition`。
:::

```text
CREATE PARTITION [IF NOT EXISTS] partition_name ON TABLE table_name;
```

对象名使用 SQL 标识符，不能用值参数代替。操作成功返回更新计数 0。

<span id="partition" />

<span id="alias" />
