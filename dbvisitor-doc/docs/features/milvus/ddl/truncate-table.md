---
id: truncate-table
sidebar_position: 7
title: TRUNCATE TABLE
---

:::info[说明]
对应 SDK 方法：`truncateCollection`。
:::

## 清空集合 {#truncate}

```sql
TRUNCATE TABLE books;
TRUNCATE TABLE books IN DATABASE archive_db;
```

**此操作清空指定集合的全部数据。** 它直接调用官方 `truncateCollection`，保留集合 schema、索引和别名；不是 DELETE 的别名，也不通过 DROP/CREATE 重建集合。没有 WHERE、LIMIT、分区或通配范围，`fetchSize`、`setMaxRows` 和查询 Hint 不限制清空行数。名称使用普通 SQL 标识符，不接受 `?` 值参数；默认当前连接数据库，显式 `IN DATABASE` 不改变 `Connection.getCatalog()`。

使用 `executeUpdate()` 或 `execute()`；成功返回更新计数 0，表示 SDK 确认，并非删除了零行，也不返回受影响行数、主键或结果集。该操作不可通过 JDBC 事务回滚。超时或连接中断不代表服务端未执行，驱动不追加重试、扫描实体或改用其他删除方式；操作前应确认目标范围和数据恢复方案。

原生 API 在 Milvus 2.6.11 引入，见[官方发布说明](https://milvus.io/docs/v2.6.x/release_notes.md#v2611)与[Java API](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/truncateCollection.md)。Milvus 2.6.2 返回原生 `UNIMPLEMENTED`，不能使用此命令。驱动不提供旧版本替代实现。
