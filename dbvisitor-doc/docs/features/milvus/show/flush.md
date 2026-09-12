---
id: flush
sidebar_position: 7
title: SHOW FLUSH ALL
---

:::info[说明]
对应 SDK 方法：`getFlushAllState`。
:::

```sql
SHOW FLUSH ALL ? IN DATABASE archive;
```

`SHOW FLUSH ALL timestamp` 单次调用 `getFlushAllState`，返回一行 `FLUSH_ALL_TS`（BIGINT）和 `FLUSHED`（BOOLEAN）。时间戳接受非负 BIGINT 或 `setLong()` 参数，应保留 SDK 返回的原始整数，不能转换成 Java 日期或浮点数；状态查询不触发落盘、不自动等待。false 表示尚未满足原生完成条件，SDK 未提供的状态保留 NULL，不补造完成结果。

查询的数据库范围须与 FLUSH ALL 请求一致。
