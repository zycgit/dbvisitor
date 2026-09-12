---
id: compact
sidebar_position: 7
title: COMPACT
---

:::info[说明]
对应 SDK 方法：`compact`。
:::

## 压缩任务 {#compaction}

```sql
COMPACT TABLE table_name;
COMPACT table_name WITH (is_clustering=false, is_l0=false, target_size=512);
```

`COMPACT` 提交原生异步压缩任务，返回一个 ResultSet，其中一行 `COMPACTION_ID` 为 BIGINT；用 `executeQuery()` 或 `execute()` 获取，不是更新计数，也不是 `getGeneratedKeys()`。驱动不轮询完成、不隐式 FLUSH、不把压缩当作 commit。提交失败时没有已知任务 ID 不代表服务端必然未收到请求，重提之前应确认任务状态。

WITH 可选项与 SDK 对应：`is_clustering`、`is_l0` 为布尔值，`target_size` 为正 long 整数，单位 MB；未指定时保留 SDK 默认值。特殊压缩模式和目标大小是否支持及其效果取决于服务端版本、集合配置和 SDK，不由驱动模拟。参数值可写 `?`，分别以 `setBoolean` / `setLong` 绑定。
