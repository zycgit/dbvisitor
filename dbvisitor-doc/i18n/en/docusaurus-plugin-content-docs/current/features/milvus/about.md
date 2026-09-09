---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: Milvus Features
description: Milvus vector database capability scope, API support, and vector search methods in dbVisitor.
---

# Milvus Data Source Features

dbVisitor accesses the Milvus vector database via the [JDBC-Milvus](../../drivers/milvus/about) driver, based on the JDBC protocol. Unlike the native command style of MongoDB/ElasticSearch, the Milvus adapter uses **SQL-style syntax** (`CREATE TABLE`, `INSERT`, `SELECT`, `DELETE`, etc.), resulting in a lower learning curve.

## Quick Overview of Differences

| Concern | Milvus Behavior |
|--------|------------|
| API Support | JdbcTemplate, Builder API, BaseMapper, Annotations, Mapper File |
| Primary Key Generation | JDBC INSERT/UPSERT supports `RETURN_GENERATED_KEYS`, exposing SDK Int64/VarChar IDs |
| Pagination | LIMIT/OFFSET and maxRows bound results; fetchSize controls on-demand page size |
| Multi-row Writes | Multiple VALUES or Iterable/Iterator inputs; JDBC executeBatch is unsupported |
| Stored Procedures | Not supported |
| Vector Search | Type-compatible KNN/range search; Hybrid fuses candidates into one result set |

**Unsupported:** transactions/savepoints, JDBC executeBatch, stored procedures and updatable ResultSets. API usage remains subject to the driver's SQL subset; arbitrary generic operations or SQL are not guaranteed.

## Concept Analogy

The Milvus adapter uses a SQL-style command subset:

- **DDL** — `CREATE TABLE`, `DROP TABLE`, `CREATE INDEX`, executed via `executeUpdate`
- **DML** — `INSERT`, `UPDATE`, `DELETE`, obtain affected row count via `executeUpdate`
- **DQL** — `SELECT` queries return standard `ResultSet`

:::info[Milvus Special Requirements]
Create the required index and execute `LOAD TABLE table_name` before querying. UPDATE selects keys page by page and uses native Partial Upsert for SET fields only. Without LIMIT it continues over matching entities, but provides no cross-page transaction, whole-operation rollback or exactly-once guarantee.
:::

## Detailed Usage

For complete JdbcTemplate, Builder API, BaseMapper, Annotation, and Mapper File usage, see the [Milvus Usage Guide](./usage).

## Core Topics

- [Vector Search](./usage#vector-search): single-vector KNN, the L2 range builder and SQL threshold rules for COSINE/IP.
- [Filtered vector search](./usage#hybrid-query): distinct from [native multi-path Hybrid fusion](../../drivers/milvus/commands.md#hybrid).
- [Consistency Level](./usage#consistency): `consistencyLevel=Strong` for immediate visibility

## Relationship to General Documentation

For general API usage, see [Core API](../../guides/overview). For vector query API, see [Vector Queries](../../guides/core/vector_query/about).

Current source requires Java 17+, SDK 2.6.22 and a minimum Milvus 2.6.2 baseline. See the [Versions and Support](../../drivers/milvus/compatibility.md) for version and feature requirements, and [typed values and generated keys](../../drivers/milvus/usecase.mdx#typed-values) for JDBC examples.
