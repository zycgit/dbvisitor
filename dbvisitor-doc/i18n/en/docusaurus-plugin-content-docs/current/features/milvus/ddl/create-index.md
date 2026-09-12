---
id: create-index
slug: /features/milvus/sql/indexes
sidebar_position: 8
title: CREATE INDEX
---

:::info[Note]
SDK methods: `createIndex`.
:::

## Create Index

### Syntax

```text
CREATE INDEX [index_name] ON [TABLE] table_name (field_name)
    [USING index_type]
    [WITH (option_name = option_value, ...)];
```

Each statement creates an index on one field; multi-field composite indexes and `IF NOT EXISTS` are not supported. When `index_name` is omitted, naming is left to the SDK and server. Specify a name when you need to alter or drop the index later. `index_type` accepts an identifier or string, such as `HNSW` or `'HNSW'`. Collection, field and index names cannot be bound with `?`.

### Example

```sql
CREATE INDEX index_name ON TABLE table_name (vector_col) USING 'IVF_FLAT' WITH (nlist = 1024, metric_type = 'L2');
```

### Index Options and Boundaries

USING is resolved against the SDK IndexType enum; omission keeps SDK AUTOINDEX. Common FloatVector types include FLAT, IVF_FLAT, IVF_SQ8, IVF_PQ, HNSW, SCANN, DISKANN and AUTOINDEX. Common scalar types include STL_SORT, TRIE, INVERTED and BITMAP. RNSG and ANNOY are not supported.

Binary indexes include BIN_FLAT/BIN_IVF_FLAT; sparse indexes include SPARSE_INVERTED_INDEX/SPARSE_WAND. The server validates index availability against its version, the field type, and the runtime environment.

Index WITH maps metric_type (or metric) to the metric and other entries to extraParams. Values support strings, integers, decimals, booleans, identifiers and scalar ? bindings, not inline nested JSON objects. Special formats must follow the SDK string contract. Index metrics must match query distance operators.

CREATE INDEX currently waits synchronously with an SDK wait limit of 600000ms. It does not consume the IMPORT/LOAD/RELEASE sync/timeout hints.

A successful statement returns JDBC update count `0`, not the number of indexed entities. Creating an index does not replace loading the collection; see [Loading, Maintenance and Status](../admin/load.md) for search prerequisites.

<span id="index" />

<span id="index-metadata" />
