---
id: create-table
slug: /features/milvus/sql/collections
sidebar_position: 4
title: CREATE TABLE
---

:::info[Note]
SDK methods: `createCollection`.
:::

## Create Table (Collection)

### Syntax

```text
CREATE TABLE [IF NOT EXISTS] table_name (
    field_definition [, field_definition ...]
    [, function_definition ...]
) [WITH (option_name = option_value, ...)];

field_definition:
    field_name data_type [constraint ...]
    [WITH (property_name = property_value, ...)]
```

Field definitions must precede function definitions. See [Data Types and Field Constraints](../types/fields.md) for types, dimensions, Array capacity and constraints, and [Functions](functions.md) for `function_definition`. Field-level WITH and collection-level WITH have different scopes and are not interchangeable.

`table_name` identifies a collection in the connection's current database. `IF NOT EXISTS` skips creation when the collection exists, without validating or changing its schema. It is not schema synchronization or migration. Successful creation or skipping returns JDBC update count `0`. This command neither creates indexes nor loads the collection.

### Example

```sql
CREATE TABLE example_vectors (
    id INT64 PRIMARY KEY,
    vector_col FLOAT_VECTOR(2),
    age INT32 DEFAULT 0
) WITH (
    consistency_level = "Strong"
);
```


CREATE TABLE WITH accepts the following options, whose values can be bound with `?`. Unknown or duplicate names, NULL, and incorrect types produce errors rather than being silently ignored. Bind field, function, and collection-option parameters in their SQL order. DQL WITH and connection-property rules do not apply to DDL.

| Creation option | SDK field | Type and default |
| --- | --- | --- |
| `consistency_level` | consistencyLevel | Strong / Bounded / Session / Eventually; SDK default Bounded. |
| `num_partitions` | numPartitions | Positive INT32 integer; requires a PARTITION KEY. If omitted, the server chooses its default partition count. |
| `num_shards` | numShards | Positive INT32 integer; SDK default 1. The server validates its supported range. |
| `description` | description | String, empty by default; bind with setString or setObject. It is not executed as SQL. |


## Partition and Clustering Keys {#collection-keys}

```sql
CREATE TABLE tenant_books (
    id INT64 PRIMARY KEY,
    tenant VARCHAR(64) PARTITION KEY,
    age INT32 CLUSTERING KEY,
    v FLOAT_VECTOR(2)
) WITH (num_partitions=4, num_shards=1, consistency_level=Strong);
```

`PARTITION KEY` maps to SDK `isPartitionKey`, using a non-primary, non-nullable INT64 or VARCHAR field. Each collection allows at most one partition key. Milvus assigns physical partitions from key values; the driver does not calculate routing. `num_partitions` is not a tenant or host count. Filter by bound key values in WHERE; do not treat partitions as an authorization boundary. Manual partition operations in partition-key mode are subject to server restrictions; see [Partition Keys](https://milvus.io/docs/v2.6.x/use-partition-key.md).

`CLUSTERING KEY` maps to `isClusteringKey`. Each collection allows at most one, which can share a field with the partition key. Supported scalar types and compaction requirements are determined by the SDK/server. Setting the flag does not trigger compaction or guarantee a performance gain. The existing `COMPACT ... WITH (is_clustering=true)` submits a native task, but the cluster needs clustering compaction and pruning configured; see [Clustering Compaction](https://milvus.io/docs/v2.6.x/clustering-compaction.md). ADD COLUMN cannot introduce either key after creation.

SHOW TABLE appends Boolean `PARTITION_KEY` and `CLUSTERING_KEY` columns. SHOW CREATE preserves these constraints and the partition count, shard count, consistency, and description returned by the server. It is not a complete backup script containing indexes, permissions, and arbitrary collection properties.

<span id="table" />

<span id="truncate" />

<span id="rename" />
