---
id: flush
sidebar_position: 4
title: FLUSH
---

:::info[说明]
对应 SDK 方法：`flush`、`flushAll`、`getFlushAllState`。
:::

## Flush {#flush}

```sql
FLUSH table_name;
FLUSH first_table, second_table IN DATABASE archive WITH (wait_flushed_timeout_ms=60000);
FLUSH ALL TABLES;
FLUSH ALL TABLES IN DATABASE archive WITH (wait_flushed_timeout_ms=?);
```

`FLUSH table_name[, ...]` 一次提交 SDK `FlushReq.collectionNames`，等待后返回更新计数 0。`FLUSH ALL TABLES` 调用 SDK `flushAll`，**同样同步等待完成**，成功返回一行 `FLUSH_ALL_TS`（BIGINT）。使用 `executeQuery()` 或 `execute()` 取得该结果，不是 `getGeneratedKeys()`，也不是提交即返回的异步任务。`FLUSH all` 仍表示刷新名为 `all` 的单个集合，不是全库操作。

两种命令默认当前连接数据库，可用 `IN DATABASE db_name` 显式指定其他库，不改变连接的 catalog。只有 `FLUSH ALL TABLES IN DATABASE *` 才请求所有数据库；状态查询使用相同作用域，例如 `SHOW FLUSH ALL ? IN DATABASE *`。集合名和库名必须是非空 SQL 标识符，不能绑定 `?`；按集合刷新不接受数据库通配符。

`WITH` 只接受 `wait_flushed_timeout_ms`，可绑定非负 BIGINT 范围内的整数。默认沿用原有 60000ms 等待上限；显式 0 表示 SDK 无限等待，请谨慎使用。该参数控制 SDK 的完成等待，不是整个 JDBC 调用的总耗时上限；提交 RPC、网络和 SDK 重试还受连接配置影响。不消费 sync/timeout Hint，不支持用 `sync=false` 改为异步。

**2.6.2 的按库 FlushAll 完成等待存在服务端限制**：其 `GetFlushAllState` 虽然筛选了目标数据库，但检查 checkpoint 时仍遍历全部数据库，可能被其他库拖住并最终超时，见[官方 2.6.2 实现](https://github.com/milvus-io/milvus/blob/v2.6.2/internal/datacoord/services.go#L1519)。驱动和官方 SDK 的等待都受此影响。该基线可使用显式集合列表的 FLUSH；全实例通配作用域也不能绕过该等待限制。其他版本需确认原生修复情况。

FLUSH 不是 JDBC commit，也不是跨语句事务边界。失败、超时或取消不表示服务端未落盘；SDK 等待失败时可能没有可返回的时间戳。驱动不通过逐集合补刷、额外轮询或伪造成功兜底；也不在 SDK 之外自动重提。频繁刷新会受服务端限流影响。
