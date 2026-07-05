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
| Primary Key Generation | `RETURN_GENERATED_KEYS` not supported (generate PKs application-side) |
| Pagination | `LIMIT ? OFFSET ?` |
| Batch Writes | executeBatch not supported |
| Stored Procedures | Not supported |
| Vector Search | KNN nearest neighbor search (L2/Cosine/IP) + range search |

**Not supported:** executeBatch, Stored Procedures, `Statement.RETURN_GENERATED_KEYS`

## Concept Analogy

The Milvus adapter uses standard SQL-style syntax:
- **DDL** — `CREATE TABLE`, `DROP TABLE`, `CREATE INDEX`, executed via `executeUpdate`
- **DML** — `INSERT`, `UPDATE`, `DELETE`, obtain affected row count via `executeUpdate`
- **DQL** — `SELECT` queries return standard `ResultSet`

:::info[Milvus Special Requirements]
Milvus requires collections to be **loaded into memory** before querying; first execute `LOAD TABLE table_name`. Updates are essentially "Search-to-Upsert"; full-table Updates are not recommended.
:::

## Detailed Usage

For complete JdbcTemplate, Builder API, BaseMapper, Annotation, and Mapper File usage, see the [Milvus Usage Guide](./usage).

## Core Topics

- [Vector Search](./usage#vector-search): KNN nearest neighbor search (`orderByL2`/`orderByCosine`/`orderByIP`) + range search (`vectorByL2`/`vectorByCosine`/`vectorByIP`)
- [Hybrid Queries](./usage#hybrid-query): Scalar filtering + vector search
- [Consistency Level](./usage#consistency): `consistencyLevel=Strong` for immediate visibility

## Relationship to General Documentation

For general API usage, see [Core API](../../guides/overview). For vector query API, see [Vector Queries](../../guides/core/vector_query/about).
