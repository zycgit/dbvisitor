---
id: limitations
sidebar_position: 4
title: Usage Limitations
---

## JDBC Interfaces

JDBC Batch, transactions, savepoints and updatable ResultSets are not supported. Multiple commands and multi-row writes are not JDBC Batch. Metadata is partial; check the [shared JDBC interface limitations](../limited.md) before integrating an ORM, connection pool or migration tool.

## Database-Specific Restrictions

Redis Cluster connections cannot select a nonzero database through `database`. The default client does not enable TLS; configure a custom client with `customJedis` when needed.

For exact command syntax and result semantics, see the [command reference](../../features/redis/commands.md).
