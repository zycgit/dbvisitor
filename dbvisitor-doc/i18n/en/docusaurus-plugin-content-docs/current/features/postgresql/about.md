---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: PostgreSQL
description: PostgreSQL dialect capabilities, primary key backfill, ON CONFLICT, RETURNING, sequences, and type mapping differences in dbVisitor.
---

# PostgreSQL

PostgreSQL can use all of dbVisitor's general capabilities including JDBC, Mapper, Lambda, BaseMapper, transactions, functions, and sequences.

## Quick Overview of Differences

| Concern | PostgreSQL Behavior |
|--------|----------------|
| Primary Key Generation | `SERIAL`/`BIGSERIAL`/`IDENTITY`, supports JDBC generated keys and `RETURNING` |
| Pagination | `LIMIT ? OFFSET ?` |
| Write Conflicts | `ON CONFLICT DO NOTHING` (ignore) / `ON CONFLICT ... DO UPDATE` (update) |
| Batch Writes | JDBC batch supported |
| Stored Procedures | Functions and procedures supported |
| Sequences | Supports `nextval()` |
| Vector Search | Supports pgvector (`<->`, `<=>`, `<#>` operators) |

## Primary Key Backfill

PostgreSQL supports two primary key backfill methods:

1. **JDBC generated keys**: applicable to `SERIAL`/`IDENTITY` columns, similar to MySQL
2. **RETURNING clause**: `INSERT ... RETURNING id`, read from the current ResultSet

For multi-row inserts via Lambda / BaseMapper, the PostgreSQL dialect can generate `VALUES (...), (...) RETURNING id`, returning multiple primary keys at once. For hand-written SQL (Mapper XML/Annotation), `RETURNING` must be explicitly written with `generatedKeySource="resultSet"` configured.

## Write Conflict Strategies

| Strategy | PostgreSQL Implementation |
|------|----------------|
| Ignore | `INSERT INTO ... ON CONFLICT DO NOTHING` |
| Update | `INSERT INTO ... ON CONFLICT(pk) DO UPDATE SET ...` |

`ON CONFLICT DO UPDATE` only updates non-conflict key columns to avoid writing primary key columns into `SET`.

## Sequences

PostgreSQL sequences can be used in object mapping via `KeyType.Sequence`:

```java
@KeySeq("user_info_id_seq")
@Column(value = "id", primary = true, keyType = KeyType.Sequence)
private Integer id;
```

You can also use `selectKey` in Mapper XML to read `nextval('seq')`.

## Vector Search

If the pgvector extension is installed, dbVisitor's Builder API supports vector similarity queries. See [Vector Queries](../../guides/core/vector_query/about) for details.

## Special Topics

- [Auto-generated Key Backfill](./generated-keys): Detailed configuration for `SERIAL`, `RETURNING`, `ON CONFLICT`, and `selectKey`.
- [Dialect Details](./dialect-details): Low-level implementation of pagination SQL, multi-row RETURNING, conflict strategies, sequences, and vector search.

## Relationship to General Documentation

For general API usage, see [Core API](../../guides/overview). The following content supplements specific differences and recommended practices for PostgreSQL.
