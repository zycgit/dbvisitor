---
id: about
sidebar_position: 1
title: Introduction
description: Elasticsearch JDBC driver setup, connections and usage.
---

`jdbc-elastic` is an independent JDBC driver. Access Elasticsearch with REST-style commands for document reads and writes, searches and index management. Use `Connection`, `PreparedStatement` and `ResultSet` directly; dbVisitor APIs are optional.

## Features

- Bind request parameters with PreparedStatement and read search results through ResultSet.
- Read and write documents, manage indexes, search, count and run `_cat` queries.
- Expand document fields into result columns and configure pre-read thresholds and cache storage.

## Get Connected

1. [Add dependencies](dependencies.mdx): Maven or Gradle configuration.
2. [Connect to the database](connection.mdx): JDBC URL, credentials and connection examples.
3. [Configure parameters](params.md): names, defaults and units.
4. [Usage limitations](limitations.md): JDBC support and database-specific restrictions.

## Before You Connect

- Requires Java 17 or later.
- Commands must use the syntax supported by this driver; arbitrary relational SQL is not translated.
- JDBC batch and transactions are not supported. Check the [shared JDBC limitations](../limited.md) before integrating a connection pool, ORM or other JDBC tool.

When using dbVisitor APIs, Elastic6 and Elastic7 dialects are available.

[Command reference](../../features/elastic/syntax/index.md) · [dbVisitor API usage](../../features/elastic/usage.mdx)

[Vector searches](../../features/elastic/vectors.mdx)
