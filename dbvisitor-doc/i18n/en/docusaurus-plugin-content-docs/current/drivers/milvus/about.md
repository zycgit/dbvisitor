---
id: about
sidebar_position: 1
title: Introduction
description: Access Milvus through JDBC and a SQL subset, including prepared writes, vector retrieval, collection management and Import jobs.
---

jdbc-milvus maps SQL-style commands to the official Milvus Java SDK or Import REST calls, exposed through `Connection`, `Statement`, `PreparedStatement` and `ResultSet`. Apache 2.0 licensed; use it standalone or with dbVisitor's JdbcTemplate, Mapper and Builder APIs.

## Main Capabilities

- Collection, database, index, partition, alias, user, role and permission management.
- Prepared INSERT/UPSERT, native Partial UPDATE and scalar/KNN/range DELETE, with bounded retries and failure progress for paged writes.
- On-demand scalar, KNN and range SELECT paging; single-vector distance ordering and server-side Hybrid Search with RRF/Weighted reranking.
- Float/Binary/FP16/BF16/Sparse vectors, nullable/DEFAULT/Array schemas and JDBC generated keys.
- BM25/TextEmbedding schema functions, multi-row writes and Import submission/status/failure inspection.
- Single-port SDK/REST connections, TLS/mTLS certificates, token/API key authentication and standard Zilliz Cloud endpoint configuration.

These are capabilities integrated into the current source. Actual availability depends on server version, indexes, configuration and permissions. This is neither an exhaustive SDK wrapper nor a performance or cloud-acceptance claim.

## Versions and Boundaries

Current source `6.7.1-SNAPSHOT`: Java 17+, Milvus Java SDK 2.6.22, minimum Milvus server baseline 2.6.2. Nullable vectors require 2.6.18+. Versions 2.5 and earlier are outside scope; no 3.0 support commitment has been established. See the [release and support matrix](./compatibility.md) for source versus published versions.

This is a SQL subset, not a relational database emulator. Transactions, JDBC batch, stored procedures, updatable ResultSets, JOIN/GROUP BY, column aliases and scalar ORDER BY are unsupported. Verify the actual JDBC interfaces and SQL used by a framework; arbitrary ORM, BI and migration tools are not automatically compatible.

## Start Here

- [Install and Use](./usecase.mdx): dependencies, a complete runnable program, paging, typed values, generated keys, Hybrid and Import.
- [Connection Parameters and TLS](./params.md): all connection properties, certificates, Cloud and custom clients.
- [Syntax Manual](./commands.md): exact syntax, parameters, result columns and JDBC multi-result access.
- [Release and Support Matrix](./compatibility.md): versions, feature requirements, validation scope and release checks.
- [dbVisitor API Usage](../../features/milvus/usage.mdx): JdbcTemplate, Builder, BaseMapper, annotations and Mapper files.
