---
id: create-table
slug: /features/milvus/sql/collections
sidebar_position: 4
title: CREATE TABLE
---

:::info[说明]
对应 SDK 方法：`createCollection`。
:::

## 创建表 (Collection)

### 语法

```text
CREATE TABLE [IF NOT EXISTS] table_name (
    field_definition [, field_definition ...]
    [, function_definition ...]
) [WITH (option_name = option_value, ...)];

field_definition:
    field_name data_type [constraint ...]
    [WITH (property_name = property_value, ...)]
```

字段定义必须位于函数定义之前。字段类型、维度、Array 容量及约束见[数据类型与字段约束](../types/fields.md)；`function_definition` 见[函数定义](functions.md)。字段级 WITH 和集合级 WITH 的作用域不同，不可混用。

`table_name` 是当前连接数据库中的集合名称。`IF NOT EXISTS` 在集合已存在时跳过创建，不校验或修改原有 schema；它不是 schema 同步或迁移命令。创建成功或跳过时返回 JDBC 更新计数 `0`。此命令不创建索引，也不加载集合。

### 示例

```sql
CREATE TABLE example_vectors (
    id INT64 PRIMARY KEY,
    vector_col FLOAT_VECTOR(2),
    age INT32 DEFAULT 0
) WITH (
    consistency_level = "Strong"
);
```


CREATE TABLE 的 WITH 接受以下选项，值可用 `?` 绑定；未知名称、重复名称、NULL 和错误类型会报错，不会被静默忽略。字段、函数、集合选项中的参数按 SQL 出现顺序绑定。不要将 DQL 的 WITH 或连接参数规则套用到 DDL。

| 创建选项 | SDK 字段 | 类型与默认值 |
| --- | --- | --- |
| `consistency_level` | consistencyLevel | Strong / Bounded / Session / Eventually，默认 SDK Bounded。 |
| `num_partitions` | numPartitions | 正 INT32 整数，必须同时声明 PARTITION KEY；省略时使用服务端默认分区数。 |
| `num_shards` | numShards | 正 INT32 整数，默认 SDK 1；最终范围由服务端校验。 |
| `description` | description | 字符串，默认空；可使用 setString 或 setObject 绑定，不作为 SQL 执行。 |


## 分区键与聚簇键 {#collection-keys}

```sql
CREATE TABLE tenant_books (
    id INT64 PRIMARY KEY,
    tenant VARCHAR(64) PARTITION KEY,
    age INT32 CLUSTERING KEY,
    v FLOAT_VECTOR(2)
) WITH (num_partitions=4, num_shards=1, consistency_level=Strong);
```

`PARTITION KEY` 映射到 SDK `isPartitionKey`，支持非主键、非 nullable 的 INT64 或 VARCHAR 字段。一个集合最多一个分区键；Milvus 根据键值分配物理分区，驱动不计算路由。`num_partitions` 不是租户数量或主机数量，查询仍通过 WHERE 绑定键值，不能依赖分区实现权限隔离。分区键模式下的手动分区操作受服务端限制，见[官方分区键说明](https://milvus.io/docs/v2.6.x/use-partition-key.md)。

`CLUSTERING KEY` 映射到 `isClusteringKey`，一个集合最多一个，可与分区键使用同一字段。支持的标量类型和实际压缩条件由 SDK/服务端决定；设置标志本身不触发压缩，也不保证性能提升。已有 `COMPACT ... WITH (is_clustering=true)` 可提交原生任务，但集群需配置聚簇压缩和数据裁剪能力，见[官方聚簇压缩说明](https://milvus.io/docs/v2.6.x/clustering-compaction.md)。不通过 ADD COLUMN 后补分区键或聚簇键。

SHOW TABLE 在末尾增加 `PARTITION_KEY`、`CLUSTERING_KEY` 布尔列；SHOW CREATE 保留这些约束以及服务端返回的分区数、分片数、一致性和说明。它不是包含索引、权限及任意集合属性的完整备份脚本。

<span id="table" />

<span id="truncate" />

<span id="rename" />
