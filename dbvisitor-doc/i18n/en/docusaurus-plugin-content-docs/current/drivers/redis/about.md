---
id: about
sidebar_position: 0
title: Introduction
description: Redis JDBC driver setup, connections and usage.
---

`jdbc-redis` is an independent JDBC driver. Access Redis strings, hashes, lists, sets and sorted sets using native commands. Use `Connection`, `PreparedStatement` and `ResultSet` directly; dbVisitor APIs are optional.

## Features

- Use common String, Hash, List, Set and Sorted Set commands.
- Bind command parameters through PreparedStatement and read responses as result sets or update counts.
- Connect to standalone Redis or Redis Cluster and configure authentication, timeouts and cluster pooling.

## Get Connected

1. [Add dependencies](dependencies.mdx): Maven or Gradle configuration.
2. [Connect to the database](connection.mdx): JDBC URL, credentials and connection examples.
3. [Configure parameters](params.md): names, defaults and units.
4. [Usage limitations](limitations.md): JDBC support and database-specific restrictions.

## Before You Connect

- Requires Java 17 or later.
- Commands must use the syntax supported by this driver; arbitrary relational SQL is not translated.
- JDBC batch and transactions are not supported. Check the [shared JDBC limitations](../limited.md) before integrating a connection pool, ORM or other JDBC tool.
- The JDBC URL prefix is `jdbc:dbvisitor:jedis://`, not `redis://`.

[Command reference](../../features/redis/syntax/index.md) · [dbVisitor API usage](../../features/redis/usage.mdx)
