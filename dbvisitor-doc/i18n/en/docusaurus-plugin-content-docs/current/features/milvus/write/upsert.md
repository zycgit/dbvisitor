---
id: upsert
sidebar_position: 2
title: UPSERT
---

:::info[Note]
SDK methods: `upsert`.
:::

## Syntax

```text
UPSERT INTO collection_name
    [PARTITION partition_name]
    [(field_name [, ...])]
    VALUES { (value [, ...]) [, ...] | ? };
```

INSERT calls native insert; UPSERT calls native upsert without a preliminary existence query. Neither implements relational unique-key conflict checks. UPSERT replaces the full entity by default; explicitly enable partial_update for partial updates.

INSERT/UPSERT accepts multiple VALUES tuples with explicit columns, or VALUES ? bound to an Iterable/Iterator of Maps (columns optional), Lists or Object[] (columns required). INSERT SELECT is unsupported. Counts reflect SDK acknowledgements; RETURN_GENERATED_KEYS additionally exposes the standard JDBC key cursor.


## Upsert (Insert or Replace) {#upsert}

```sql
-- Upsert into default partition
UPSERT INTO table_name (id, vector, age) VALUES (1, [0.1, 0.2], 10);

-- Upsert into specific partition
UPSERT INTO table_name PARTITION partition_name (id, vector) VALUES (2, [0.3, 0.4]);

-- Native partial update: change only age for existing keys; new keys follow server insert rules
/*+ partial_update=true */ UPSERT INTO table_name (id, age) VALUES (?, ?);
```

`partial_update` is a boolean UPSERT hint, optionally bound with `?`, mapped to SDK `UpsertReq.partialUpdate`. Omission or false retains full replacement; true uses Milvus 2.6.2+ native partial update, preserving omitted fields of existing entities. New keys must still satisfy required fields, defaults, and function inputs in the schema; the driver does not first query a complete entity. The hint is invalid for INSERT and rejects non-boolean values. Multiple pages are not atomic or collectively reversible, and ambiguous writes are not automatically replayed.

INSERT/UPSERT and UPDATE SET convert JSON, vectors and Array according to schema. JSON strings, Maps, Lists, JsonElements and numeric arrays are encoded as JSON without FloatVector precision conversion. This is not a lossless guarantee for arbitrary inputs; SDK/server validation still applies.

Multi-row binding and generated keys: [INSERT](insert.md).
