---
id: health
sidebar_position: 15
title: SHOW HEALTH
---

:::info[说明]
对应 SDK 方法：`checkHealth`。
:::

```sql
SHOW HEALTH;
```

| 语句 | 返回列 |
| --- | --- |
| SHOW HEALTH | IS_HEALTHY（BOOLEAN）、REASONS、QUOTA_STATES（后两列为 JSON 数组文本 / VARCHAR） |

使用 `executeQuery()` 或 `execute()` 读取结果集。此命令只读取服务端状态，不加载集合、刷新数据或扫描实体。

IS_HEALTHY=false 时仍可读取原因和限流状态；认证或 RPC 失败则抛出 SQLException。
