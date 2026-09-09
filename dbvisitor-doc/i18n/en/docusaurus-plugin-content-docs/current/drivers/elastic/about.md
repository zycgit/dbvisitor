---
id: about
sidebar_position: 1
title: Introduction
description: jdbc-elastic JDBC capabilities, architecture, dependencies and documentation.
---

## Introduction
jdbc-elastic is a JDBC driver adapter for Elasticsearch. It allows developers to operate Elasticsearch using standard JDBC interfaces and native REST-style commands.

Core value:
- Use standard JDBC APIs (Connection, Statement, PreparedStatement, ResultSet).
- Use native REST-style command text that maps to Elasticsearch operations.
- Provide a unified programming style for heterogeneous data sources via dbVisitor.

## Features
- Implements the JDBC core interfaces and supports `PreparedStatement` placeholders.
- Supports REST-style commands and multiple commands separated by semicolons.
- Supports search/count/multi-search/multi-get, document CRUD, index management, and `_cat` queries.
- Supports `HEAD` requests and returns a `STATUS` column.
- Result mapping: search-like responses map to `_ID` and `_DOC` columns; pre-read expands fields as columns.
- Pre-read mode for large result sets with configurable threshold, max file size, and cache directory.
- Optional `indexRefresh` to append `refresh=true` for write operations.
- Elasticsearch 6/7 scenarios are covered by dbVisitor dialects and realdb tests (see `Elastic6Dialect`, `Elastic7Dialect`, and `realdb/elastic6|elastic7`).

## JDBC and Implementation

ANTLR4 parses commands and the official client executes them. The shared JDBC layer handles Connection, Statement, PreparedStatement, ResultSet and type conversion. Multiple commands and JDBC multi-result access do not imply arbitrary ORM SQL, transactions, JDBC batch or complete DatabaseMetaData. ResultSets are read-only and forward-only; compatible values support getInt/getString and BLOB/CLOB/NCLOB reads. INSERT can expose adapter-returned `_id` values through getGeneratedKeys, not a guarantee for every generic Mapper backfill path.

## Compatibility
- JDK 17+
- Elasticsearch REST client: `elasticsearch-rest-client` 7.17.10
- Jackson: `jackson-databind` 2.18.0
- dbVisitor includes Elastic6/Elastic7 dialects and realdb test suites for ES6/ES7 scenarios.

## Documentation

- [Install and Use](./usecase.mdx): dependencies, JDBC connections, prepared operations and multiple results.
- [Connection Parameters](./params.md): authentication, timeouts, custom clients and pre-read.
- [Command Reference](./commands.md): coverage, hints and limitations.
- [Vector Search Guide](./vectors.mdx): mapping, Lambda, native DSL, parameters and tuning.
- [dbVisitor APIs](../../features/elastic/usage.mdx): JdbcTemplate, Mapper and Builder usage.
