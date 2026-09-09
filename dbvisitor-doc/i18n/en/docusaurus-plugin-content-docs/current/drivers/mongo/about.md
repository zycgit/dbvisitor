---
id: about
sidebar_position: 1
title: Introduction
description: jdbc-mongo JDBC capabilities, architecture, dependencies and documentation.
---

## Introduction
jdbc-mongo is a JDBC driver adapter for MongoDB. It allows developers to operate MongoDB using standard JDBC interfaces and native-command style Mongo commands.

Core value:
- Use standard JDBC APIs (Connection, Statement, PreparedStatement, ResultSet).
- Use native-command style command text that maps to MongoDB operations.
- Provide a unified programming style for heterogeneous data sources via dbVisitor.

## Features
- Implements the JDBC core interfaces and supports `PreparedStatement` placeholders.
- Supports native-command style Mongo commands and multiple commands separated by semicolons.
- Supports collection, index, user, and database management commands.
- `find` supports method chaining: `limit(...)`, `skip(...)`, `sort(...)`, `hint(...)`.
- Result mapping: `find` returns `_ID` and `_JSON` columns; when pre-read is enabled, document fields are also expanded as columns.
- Pre-read mode for large result sets with configurable threshold, max file size, and cache directory.

## JDBC and Implementation

ANTLR4 parses commands and the official client executes them. The shared JDBC layer handles Connection, Statement, PreparedStatement, ResultSet and type conversion. Multiple commands and JDBC multi-result access do not imply arbitrary ORM SQL, transactions, JDBC batch or complete DatabaseMetaData. ResultSets are read-only and forward-only; compatible values support getInt/getString and BLOB/CLOB/NCLOB reads. INSERT can expose adapter-returned `_id` values through getGeneratedKeys, not a guarantee for every generic Mapper backfill path.

## Compatibility
- JDK 17+
- MongoDB Java driver: `mongodb-driver-sync` 5.6.1 (compatible with the server versions supported by this driver)

## Documentation

- [Install and Use](./usecase.mdx): dependencies, JDBC connections, prepared operations and multiple results.
- [Connection Parameters](./params.md): authentication, timeouts, custom clients and pre-read.
- [Command Reference](./commands.md): coverage, hints and limitations.
- [dbVisitor APIs](../../features/mongo/usage.mdx): JdbcTemplate, Mapper and Builder usage.
