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
结果是一行 `COUNT`（BIGINT），用 `rs.getLong("COUNT")` 获取；空匹配返回 0。可在末尾用 WITH 指定[查询级选项](select.md#query-options)，`ignore_growing=true` 计数同样排除 growing segments。

```sql
-- 查询全表总数
count from table_name;
SELECT COUNT(*) FROM table_name;

-- 查询指定分区的总数
COUNT FROM table_name PARTITION partition_name;

-- 带条件过滤的总数 (支持标量过滤)
count from table_name where age > 18;
```

## 聚合与去重限制 {#aggregate-limits}

支持对标量条件的匹配记录执行 `COUNT(*)`；构造器 API 的 `queryForCount()` 也可使用。

不支持 `COUNT(field)`、`COUNT(DISTINCT field)`、`DISTINCT`、`GROUP BY`、`SUM` 或 `MAX`，也不能在同一查询中混合统计值与普通字段。这些限制同样适用于构造器 API 生成的语句。

向量范围查询不能使用 COUNT。需要将相似度搜索结果按类别分组时，使用[向量分组搜索](select.md#grouping)；它返回匹配实体，不返回每组的统计值。
