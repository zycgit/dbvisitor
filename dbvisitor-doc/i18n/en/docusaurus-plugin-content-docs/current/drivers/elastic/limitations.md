---
id: limitations
sidebar_position: 4
title: Usage Limitations
---

## JDBC Interfaces

JDBC Batch, transactions, savepoints and updatable ResultSets are not supported. Multiple commands and multi-row writes are not JDBC Batch. Metadata is partial; check the [shared JDBC interface limitations](../limited.md) before integrating an ORM, connection pool or migration tool.

## Database-Specific Restrictions

Requests use REST-style commands; arbitrary relational SQL is not automatically translated into Elasticsearch queries. Pre-reading expands document fields by default; when disabled, read the dedicated result columns such as `_ID` and `_DOC`.

For exact command syntax and result semantics, see the [command reference](../../features/elastic/commands.md).

The `clientName` connection parameter has no effect. Use `customElastic` for advanced client configuration.
