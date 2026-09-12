---
id: update
slug: /features/milvus/sql/mutations
sidebar_position: 3
title: UPDATE
---

:::info[说明]
对应 SDK 方法：`query`、`queryIterator`、`search`、`searchIteratorV2`、`upsert`。
:::

## 语法

```text
UPDATE collection_name [PARTITION partition_name]
    SET field_name = value [, ...]
    [WHERE condition]
    [ORDER BY vector_field distance_operator query_vector]
    [LIMIT row_count];
```

## 更新数据 (UPDATE)

> **注意**：UPDATE 使用 Milvus 2.6.2+ 的原生 Partial Update。驱动分页查询主键，每页只发送主键及 SET 字段，不回写未修改字段，不会将整个选择集合一次性积攒到内存。跨页不提供事务或整体回滚；同一 SET 字段的并发修改仍由 Milvus 的写入语义决定。

### 1. 基础更新 (标量过滤)

使用本手册的标量 WHERE 语法过滤。省略 WHERE 且无向量条件时会更新整个集合；按需求明确条件或 LIMIT。不能修改主键。SET 是值赋值，支持常量/参数，不支持 `age = age + 1` 这种逐行计算。

```sql
-- 按主键更新
UPDATE table_name SET age = 20 WHERE id = 1;

-- 按标量条件选取主键，再提交 Partial Update
UPDATE table_name SET status = 'active' WHERE age > 18;
```

### 2. 最近邻更新 (KNN Update)

更新距离目标向量最近的 K 条记录。
底层机制：按距离迭代选取主键 -> 逐页 Partial Upsert。

```sql
-- 将距离 [0.1, 0.2] 最近的 1 条记录的 status 更新为 1
UPDATE table_name SET status = 1 ORDER BY vector_col <-> [0.1, 0.2] LIMIT 1;
```

### 3. 范围更新 (Range Update)

更新所有落在目标向量指定距离（半径）内的记录。
可以使用 `vector_range` 函数或 `<->` 比较表达式。

```sql
-- 使用 vector_range 函数 (推荐)
-- 语法: vector_range(vector_field, target_vector, radius)
UPDATE table_name SET tag = 'A' WHERE vector_range(vector_col, [0.1, 0.2], 0.5);

-- 使用比较表达式
UPDATE table_name SET tag = 'A' WHERE vector_col <-> [0.1, 0.2] < 0.5;
```


## 分页写入、取消和重试

- UPDATE/DELETE 的 LIMIT 来自 SQL 本身，SELECT 的 overwrite_find_limit/skip Hint 和 JDBC maxRows 不限制 DML 写入量。向量 UPDATE/DELETE 的显式 LIMIT 当前至多 Integer.MAX_VALUE，标量 LIMIT 使用 long；无 LIMIT 仍表示无主动总量截断。
- UPDATE、带 LIMIT 的标量 DELETE、KNN/范围 DELETE 使用迭代器分页。无 LIMIT 不设置任意固定总条数；无 LIMIT 的标量 DELETE 直接使用服务端过滤删除。
- `Statement.setFetchSize(n)` 控制页大小，不能代替 LIMIT；未设置时使用 SDK 页大小，超出 SDK 单页上限时按 SDK 上限取页。
- `Statement.cancel()` 或查询超时生效后，不再主动读取下一页或发起后续写入/重试，并关闭迭代器。在途 RPC 和已成功页面不承诺撤销。
- JDBC 连接参数 `maxRetry` 是初次写入失败后的最大重试次数；目前共享于分页 Partial Upsert 和 DELETE 写调用。重试耗尽时可能已经部分成功，不保证精确一次执行。不是所有 SDK 操作都自动重试。
- 仅重试已识别的临时错误，例如 gRPC UNAVAILABLE、RESOURCE_EXHAUSTED、ABORTED、DEADLINE_EXCEEDED、SDK 限流/服务暂不可用及 JDBC 临时连接异常；参数、权限、集合不存在和未知错误直接失败。退避从 100ms 开始翻倍，单次等待最多 1000ms；等待期间持续检查取消和查询超时。
- 读取下一页不自动重试，避免在游标位置不确定时跳过或重复数据。普通 INSERT/UPSERT、Import 创建也不自动套用该重试策略。
- 分页 DML 异常携带 `phase=read/write/close`、`iterator`、`page`、`confirmedPages`、`confirmedRows`、`currentPageRows`，并保留 SQLState、错误码和 cause。已有主异常时，迭代器关闭异常放入 suppressed，不覆盖主异常。已确认进度不是服务端最终状态的完整证明：失败中的页面仍可能有部分写入已生效。
- 更新计数（包括跨页累加）保留为 `long`，通过 `executeLargeUpdate()` / `getLargeUpdateCount()` 获取。超过 `Integer.MAX_VALUE` 时，普通 `executeUpdate()` / `getUpdateCount()` 沿用公共驱动策略返回 `Statement.SUCCESS_NO_INFO`，不将计数截断或使已完成的分页操作报整数溢出错误。

<span id="dml" />
