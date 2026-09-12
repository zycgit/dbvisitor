---
id: version
sidebar_position: 14
title: SHOW VERSION
---

:::info[说明]
对应 SDK 方法：`getServerVersionV2`。
:::

```sql
SHOW VERSION;
```

| 语句 | 返回列 |
| --- | --- |
| SHOW VERSION | VERSION、BUILD_TIME、GIT_COMMIT、GO_VERSION、DEPLOY_MODE，均为 VARCHAR |

使用 `executeQuery()` 或 `execute()` 读取结果集。此命令只读取服务端状态，不加载集合、刷新数据或扫描实体。

返回一行，构建详情取决于服务端版本。
