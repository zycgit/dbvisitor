---
id: insert
slug: /features/milvus/sql/insert
sidebar_position: 1
title: INSERT
---

:::info[Note]
SDK methods: `insert`.
:::

## Syntax

```text
INSERT INTO collection_name
    [PARTITION partition_name]
    [(field_name [, ...])]
    VALUES { (value [, ...]) [, ...] | ? };
```

INSERT calls native insert; UPSERT calls native upsert without a preliminary existence query. Neither implements relational unique-key conflict checks. UPSERT replaces the full entity by default; explicitly enable partial_update for partial updates.

INSERT/UPSERT accepts multiple VALUES tuples with explicit columns, or VALUES ? bound to an Iterable/Iterator of Maps (columns optional), Lists or Object[] (columns required). INSERT SELECT is unsupported. Counts reflect SDK acknowledgements; RETURN_GENERATED_KEYS additionally exposes the standard JDBC key cursor.


## Insert Data

```sql
-- Insert into default partition
INSERT INTO table_name (id, vector, age) VALUES (1, [0.1, 0.2], 10);

-- Insert into specific partition
INSERT INTO table_name PARTITION partition_name (id, vector) VALUES (2, [0.3, 0.4]);
```


## Multi-row writes and generated keys {#generated-keys}

```sql
INSERT INTO docs (id,body,dense) VALUES (1,'first',[1,2]),(2,'second',[3,4]);
UPSERT INTO docs (id,body,dense) VALUES (1,'changed',[1,2]),(3,'third',[3,4]);
INSERT INTO docs (id,body,dense) VALUES ?;
UPSERT INTO docs VALUES ?;
```

Bind Iterable/Iterator to VALUES ?: rows are Lists/Object[] with column names, or Maps whose keys match the named columns. Omitted column lists require Maps. The SDK handles omitted defaults/nullable/function output fields. fetchSize controls entities per request, zero uses the SDK default, oversized values are capped at the SDK page maximum, and total rows are not capped. Caller-owned iterators/streams are not closed.

Counts use actual SDK long acknowledgements. Request keys using prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) or Statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS). Keys contain SDK IDs under the primary field name. Default/NO_GENERATED_KEYS yields an empty cursor; maxRows does not constrain writes/keys.

Ordinary INSERT/UPSERT is not automatically retried. Failure includes phase, confirmedPages, confirmedRows and currentPageRows; confirmed pages are not rolled back and unacknowledged writes may have taken effect. Keys are delivered on successful completion; requesting all keys requires O(number of keys) memory.

<span id="upsert" />
