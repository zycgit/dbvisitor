---
id: limitations
sidebar_position: 4
title: Usage Limitations
---

## JDBC Interfaces

JDBC Batch, transactions, savepoints and updatable ResultSets are not supported. JdbcTemplate batch operations can execute commands individually; this is neither JDBC Batch nor a transaction. See the [shared JDBC interface limitations](../limited.md) for other interfaces.

`DatabaseMetaData.getTables()` returns collections and views, using the database name as the catalog. `getColumns()` does not infer field types by sampling documents; fixed column metadata is not provided.

## Database-Specific Restrictions

The database path in the URL is also used as the default authentication database. Selecting X-509 authentication does not enable TLS. Configure connection timeouts through `customMongo`; the `connectTimeout` connection property has no effect.

For exact command syntax and result semantics, see the [command reference](../../features/mongo/about.md).
