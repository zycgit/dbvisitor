---
id: compaction
sidebar_position: 10
title: SHOW COMPACTION
---

:::info[说明]
对应 SDK 方法：`getCompactionState`、`getCompactionPlans`。
:::

```sql
SHOW COMPACTION ?;
SHOW COMPACTION PLANS ?;
```

`SHOW COMPACTION ?` 接受 `setLong` 绑定的任务 ID，返回一行 `COMPACTION_ID`、`STATE`、`EXECUTING_PLANS`、`COMPLETED_PLANS`、`TIMEOUT_PLANS`。STATE 是 SDK 状态名称，后三列为 BIGINT 计划数量，不是百分比或受影响行数。

`SHOW COMPACTION PLANS ?` 返回一行 `COMPACTION_ID`、`STATE`、`PLANS`；PLANS 是 VARCHAR JSON 数组，每个计划包含 `sources` 源段 ID 列表和 `target` 目标段 ID。即使没有合并计划，也保留状态行并返回 `[]`。两次 SHOW 是独立快照，结果可能随任务推进变化；不存在的任务或服务端错误作为 SQLException 返回。SQL 不扩大 SDK 本身提供的状态或失败信息。
