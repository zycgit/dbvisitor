---
id: count
sidebar_position: 2
title: COUNT / SELECT COUNT(*)
---

:::info[说明]
对应 SDK 方法：`query`。
:::

## 语法

```text
{ COUNT | SELECT COUNT(*) } FROM collection_name
    [PARTITION partition_name] [WHERE scalar_condition]
    [WITH (option = value [, ...])];
```

## 统计总数 (Count)

使用 `COUNT FROM ...` 或 `SELECT COUNT(*) FROM ...` 查询集合或分区的记录总数，两者映射同一个 Milvus 原生计数请求，不遍历客户端结果来计数。
结果是一行 `COUNT`（BIGINT），用 `rs.getLong("COUNT")` 获取；空匹配返回 0。可在末尾用 WITH 指定[查询级选项](select.md#query-options)，`ignore_growing=true` 计数同样排除 growing segments。不支持 COUNT(field)、混合字段投影、GROUP BY 或向量范围条件。

```sql
-- 查询全表总数
count from table_name;
SELECT COUNT(*) FROM table_name;

-- 查询指定分区的总数
COUNT FROM table_name PARTITION partition_name;

-- 带条件过滤的总数 (支持标量过滤)
count from table_name where age > 18;
```
