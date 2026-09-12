---
id: create-index
slug: /features/milvus/sql/indexes
sidebar_position: 8
title: CREATE INDEX
---

:::info[说明]
对应 SDK 方法：`createIndex`。
:::

## 创建索引

### 语法

```text
CREATE INDEX [index_name] ON [TABLE] table_name (field_name)
    [USING index_type]
    [WITH (option_name = option_value, ...)];
```

一次语句为一个字段创建索引，不支持多字段组合索引，也不提供 `IF NOT EXISTS`。`index_name` 省略时由 SDK 和服务端确定名称；需要后续修改或删除时，建议显式指定。`index_type` 可写为标识符或字符串，例如 `HNSW` 或 `'HNSW'`。集合名、字段名和索引名不能通过 `?` 绑定。

### 示例

```sql
CREATE INDEX index_name ON TABLE table_name (vector_col) USING 'IVF_FLAT' WITH (nlist = 1024, metric_type = 'L2');
```

### 索引选项与支持边界

USING 按 SDK 的 IndexType 枚举解析，省略时沿用 SDK 的 AUTOINDEX。常用 FloatVector 类型包括 FLAT、IVF_FLAT、IVF_SQ8、IVF_PQ、HNSW、SCANN、DISKANN、AUTOINDEX；常用标量类型包括 STL_SORT、TRIE、INVERTED、BITMAP。不支持 RNSG、ANNOY。

Binary 索引包括 BIN_FLAT/BIN_IVF_FLAT，Sparse 包括 SPARSE_INVERTED_INDEX/SPARSE_WAND。服务端会根据版本、字段类型和运行环境校验索引是否可用。

索引 WITH 的 metric_type（或 metric）映射距离类型，其余值作为 extraParams；支持字符串、整数、小数、布尔、标识符和标量 ?，不支持直接书写嵌套 JSON 对象。特殊格式按 SDK 参数约定传递字符串。索引 metric 必须与查询距离算子一致。

CREATE INDEX 当前同步等待，SDK 等待上限为 600000ms；这里不消费 IMPORT/LOAD/RELEASE 的 sync/timeout Hint。

成功返回 JDBC 更新计数 `0`，不是索引覆盖的实体数。创建索引不代替加载集合；搜索前的加载要求见[加载、维护与状态](../admin/load.md)。

<span id="index" />

<span id="index-metadata" />
