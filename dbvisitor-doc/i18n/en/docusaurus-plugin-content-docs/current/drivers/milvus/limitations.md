---
id: limitations
sidebar_position: 4
title: Usage Limitations
---

## JDBC Interfaces

JDBC Batch, transactions, savepoints and updatable ResultSets are not supported. Multiple commands and multi-row writes are not JDBC Batch. Metadata is partial; check the [shared JDBC interface limitations](../limited.md) before integrating an ORM, connection pool or migration tool.

## Database-Specific Restrictions

JOIN, GROUP BY, column aliases and scalar ORDER BY are unsupported. Paged UPDATE/DELETE operations are not atomic across pages; some writes may have succeeded when an error occurs. Inspect failure progress before deciding how to proceed. See [Versions and Supported Scope](../../features/milvus/compatibility.md) for server and vector feature requirements.

For exact command syntax and result semantics, see the [command reference](../../features/milvus/about.md).
