---
id: about
sidebar_position: 1
title: Introduction
description: MongoDB JDBC driver setup, connections and usage.
---

`jdbc-mongo` is an independent JDBC driver. Access MongoDB collections with commands for document reads and writes, aggregation and index management. Use `Connection`, `PreparedStatement` and `ResultSet` directly; dbVisitor APIs are optional.

## Features

- Work with collections, documents and indexes, and run aggregation queries.
- Bind parameters through PreparedStatement and use query options such as pagination and sorting.
- Retrieve generated `_id` values, expand document fields and configure pre-reading.

## Get Connected

1. [Add dependencies](./dependencies.mdx): Maven or Gradle configuration.
2. [Connect to the database](./connection.mdx): JDBC URL, credentials and connection examples.
3. [Configure parameters](./params.md): names, defaults and units.
4. [Usage limitations](./limitations.md): JDBC support and database-specific restrictions.

## Before You Connect

- Requires Java 17 or later.
- Commands must use the syntax supported by this driver; arbitrary relational SQL is not translated.
- JDBC batch and transactions are not supported. Check the [shared JDBC limitations](../limited.md) before integrating a connection pool, ORM or other JDBC tool.

[Command reference](../../features/mongo/commands.md) · [dbVisitor API usage](../../features/mongo/usage.mdx)
