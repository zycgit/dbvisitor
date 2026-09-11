---
id: about
sidebar_position: 1
title: Introduction
description: Milvus JDBC driver setup, connections and usage.
---

`jdbc-milvus` is an independent JDBC driver. Access Milvus with SQL-style commands for data reads and writes, vector searches, collection management and import jobs. Use `Connection`, `PreparedStatement` and `ResultSet` directly; dbVisitor APIs are optional.

## Features

- Manage collections, indexes, partitions, aliases and access permissions.
- Use parameterized reads and writes, generated keys, paged reads and vector searches.
- Use Hybrid Search, reranking, BM25 and Import tasks, subject to server version and configuration requirements.
- Connect with TLS, mutual certificate authentication or Zilliz Cloud credentials.

## Get Connected

1. [Add dependencies](./dependencies.mdx): Maven or Gradle configuration.
2. [Connect to the database](./connection.mdx): JDBC URL, credentials and connection examples.
3. [Configure parameters](./params.md): names, defaults and units.
4. [Usage limitations](./limitations.md): JDBC support and database-specific restrictions.

## Before You Connect

- Requires Java 17 or later.
- Commands must use the syntax supported by this driver; arbitrary relational SQL is not translated.
- JDBC batch and transactions are not supported. Check the [shared JDBC limitations](../limited.md) before integrating a connection pool, ORM or other JDBC tool.
- Requires Milvus 2.6.2 or later within the documented support range; some features require newer versions. See [Versions and Supported Scope](../../features/milvus/compatibility.md). For TLS/mTLS and Zilliz Cloud, use the [secure connection examples](./connection.mdx#tls).

[Command reference](../../features/milvus/commands.md) · [dbVisitor API usage](../../features/milvus/usage.mdx)
