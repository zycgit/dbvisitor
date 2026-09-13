---
id: limitations
sidebar_position: 4
title: Usage Limitations
---

## JDBC Interfaces

JDBC Batch, transactions, savepoints and updatable ResultSets are not supported. Multiple commands and multi-row writes are not JDBC Batch. Metadata is partial; check the [shared JDBC interface limitations](../limited.md) before integrating an ORM, connection pool or migration tool.

## Database-Specific Restrictions

The database path in the URL is also used as the default authentication database. Selecting X-509 authentication does not enable TLS. Configure connection timeouts through `customMongo`; the `connectTimeout` connection property has no effect.

For exact command syntax and result semantics, see the [command reference](../../features/mongo/about.md).
