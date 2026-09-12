---
id: delete
sidebar_position: 4
title: DELETE
---

:::info[说明]
对应 SDK 方法：`query`、`queryIterator`、`search`、`searchIteratorV2`、`delete`。
:::

## 语法

```text
DELETE FROM [TABLE] collection_name [PARTITION partition_name]
    [WHERE condition]
    [ORDER BY vector_field distance_operator query_vector]
    [LIMIT row_count];
```

## 删除数据 (DELETE)

> **注意**：无 WHERE 的 DELETE 表示删除整个集合中的记录；指定 PARTITION 时仅作用于该分区。请先确认操作范围。无 LIMIT 的标量 DELETE 直接提交服务端过滤表达式；有 LIMIT 时先分页选取主键再删除。向量 DELETE 先分页搜索主键再删除，不提供整条 SQL 的原子性或回滚。

无 WHERE 的标量 DELETE 会在驱动内部使用集合的实际主键字段构造 `主键 IS NOT NULL` 过滤条件。Milvus 主键不允许为 NULL，因此该条件覆盖全部记录，包括负数、零和字符串主键，不要求字段名为 `id`。该转换仅用于省略 WHERE 的情况，不改写用户已有的条件；也不会使用 `1=1` 兜底，Milvus 2.6.2 的原生执行路径不支持这种恒真过滤。LIMIT、分区和 JDBC fetchSize 的既有语义保持不变；fetchSize 是取数页大小，不是删除总量上限。

### 1. 基础删除 (标量过滤)

使用本手册的标量 WHERE 语法过滤删除。

```sql
-- 删除全部记录，保留集合及其 schema、索引
DELETE FROM table_name;

-- 最多删除 1000 条；不指定排序时不保证具体选中哪些记录
DELETE FROM table_name LIMIT 1000;

-- 仅删除指定分区的全部记录
DELETE FROM table_name PARTITION partition_name;

-- 按主键删除
DELETE FROM table_name WHERE id = 1;
DELETE FROM table_name WHERE id IN [1, 2, 3];

-- 按标量条件删除 (无 LIMIT 时直接调用服务端过滤删除)
DELETE FROM table_name WHERE age > 18 AND status = 'inactive';

-- 指定分区删除
DELETE FROM table_name PARTITION partition_name WHERE age > 10;
```

### 2. 最近邻删除 (KNN Delete)

使用 `ORDER BY` 指定距离排序；有 LIMIT 时最多选择 K 条，无 LIMIT 时迭代整个符合条件的搜索结果。
底层机制：按距离分页选取主键 -> 逐页删除。

```sql
-- 删除距离 [0.1, 0.2] 最近的 100 条记录
DELETE FROM table_name ORDER BY vector_col <-> [0.1, 0.2] LIMIT 100;

-- 结合标量过滤：删除 category='book' 且最相似的 10 条
DELETE FROM table_name WHERE category = 'book' ORDER BY vector_col <-> [0.1, 0.2] LIMIT 10;
```

### 3. 范围删除 (Range Delete)

删除所有落在目标向量指定距离（半径）内的记录。可以使用 `vector_range` 函数或 `<->` 比较表达式。
底层机制：范围搜索迭代选取主键 -> 逐页删除。

```sql
-- 使用 vector_range 函数 (推荐)
DELETE FROM table_name WHERE vector_range(vector_col, [0.1, 0.2], 0.5);

-- 使用比较表达式：删除所有距离 [0.1, 0.2] 小于 0.5 的记录
DELETE FROM table_name WHERE vector_col <-> [0.1, 0.2] < 0.5;

-- 结合 LIMIT 进行保护 (最多删除符合条件的 1000 条)
DELETE FROM table_name WHERE vector_range(vector_col, [0.1, 0.2], 0.5) LIMIT 1000;
```

分页、取消与重试见 [UPDATE](update.md)。

## 删除计数 {#delete-count}

简单主键条件的无 LIMIT DELETE 由服务端直接处理。Milvus 2.6.2 对 `pk = ?` 的重复删除，即使实体已不存在也可能返回 1；它计数的是提交的删除主键，而不是预查询得出的现存行数。驱动不会先查再删或把这个计数改写成关系数据库式影响行数。不要用 DELETE 返回值判断实体在删除前是否存在。参见[服务端删除实现](https://github.com/milvus-io/milvus/blob/v2.6.2/internal/proxy/task_delete.go)。
