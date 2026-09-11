---
id: overview
sidebar_position: 0
hide_table_of_contents: true
title: Data Sources
description: Explains how dbVisitor handles types, dialects, primary key backfill, functions, procedures, and proprietary capabilities for each specific data source.
---

# Data Sources

`Database Access` focuses on dbVisitor's overall capabilities such as ORM, Mapper, Lambda, transactions, and TypeHandler. `JDBC Drivers` focuses on JDBC adapters and driver-layer behavior based on `dbvisitor-driver`.

This section focuses on the usage patterns and differences for specific data sources, including:

- Quickly understanding behavioral differences of each data source (primary key generation, pagination, write conflicts, batch operations, stored procedures, etc.)
- Type mappings and recommended field types
- Primary key generation methods: auto-increment keys, sequences, `selectKey`, return keys
- Dialect features (pagination, duplicate key strategies, functions, procedures, batch writes)
- Data-source-specific SQL, DSL, or limitations
- Caveats that differ from the general API documentation

## Relational Databases

- [MySQL](./mysql/about): `AUTO_INCREMENT`, JDBC generated keys, `INSERT IGNORE`/`ON DUPLICATE KEY UPDATE`, `TINYINT(1)` boolean mapping
- [PostgreSQL](./postgresql/about): `SERIAL`, `RETURNING`, `ON CONFLICT`, pgvector vector search, sequences
- [Oracle](./oracle/about): `IDENTITY`, sequence, `keyColumn` requirement, `MERGE` conflict strategy, `RETURNING INTO` limitations
- [SQL Server](./mssql/about): `OUTPUT INSERTED`, `MERGE`, `ROW_NUMBER` pagination, `ORDER BY` deduplication
- [DB2](./db2/about): `IDENTITY`, sequence, batch generated keys limitation, `MERGE` conflict strategy
- [Dameng](./dm/about): Auto-increment columns, `IGNORE_ROW_ON_DUPKEY_INDEX`, `MERGE`, sequences
- [H2](./h2/about): `IDENTITY`, sequence, `MERGE`, recommended for testing
- [ClickHouse](./clickhouse/about): Application-side ID generation, JDBC batch, analytical write model

## Non-Relational Databases

- [Redis](./redis/about): Three modes — JdbcTemplate + Annotations + Mapper File; String/Hash/List/Set/Sorted Set operations
- [MongoDB](./mongo/about): Five modes — JdbcTemplate + Builder + BaseMapper + Annotations + Mapper File; `_id` backfill
- [ElasticSearch](./elastic/about): JdbcTemplate + Builder + BaseMapper + Annotations + Mapper File; DSL style
- [Milvus](./milvus/about): SQL-style syntax; KNN nearest neighbor search + range search; `LOAD TABLE` prerequisite

## Data Source Support Matrix

To compare support differences across data sources for Builder API, pagination, sequences, vectors, and other features, see the [Data Source Support Matrix](./support).

## Reading Suggestions

To learn dbVisitor's general capabilities, read [Core APIs](../guides/overview) first. If you're interested in JDBC driver adapters for MongoDB, Redis, Elasticsearch, Milvus, etc., read [JDBC Drivers](../drivers/about).

When a database behaves differently from the general documentation, the data-source-specific notes in this section take precedence.

## Independent JDBC use

MongoDB, Elasticsearch, Redis and Milvus can be accessed through independent [JDBC drivers](../drivers/about), without using dbVisitor APIs. For relational databases such as MySQL, use the vendor JDBC driver.
