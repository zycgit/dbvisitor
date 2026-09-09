---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: ElasticSearch Features
description: ElasticSearch data source capability scope, API support, and usage patterns in dbVisitor.
---

# ElasticSearch Data Source Features

dbVisitor accesses ElasticSearch data sources via the [JDBC-Elastic](../../drivers/elastic/about) driver, based on the JDBC protocol.

## Quick Overview of Differences

| Concern | ElasticSearch Behavior |
|--------|-------------------|
| API Support | JdbcTemplate, Builder API, BaseMapper, Annotations, Mapper File |
| Primary Key Generation | JDBC RETURN_GENERATED_KEYS returns `_id`; omit Mapper keyColumn |
| Pagination | Paginated via Page object (from/size) |
| Batch Writes | executeBatch not supported |
| Stored Procedures | Not supported |
| Vector Search | Elastic 7 (script sort/filter) / Elastic 8 (native kNN) |

**Not supported:** executeBatch, Stored Procedures

## Concept Analogy

Execution results of different ElasticSearch commands fall into two categories:
- **Update count** — analogous to INSERT/UPDATE/DELETE, obtained via `executeUpdate`
- **Single/Multi-row results** — analogous to SELECT result sets, find/search results expose `_ID` and `_DOC`; pre-read also expands document fields. Other commands have their own result columns

## Detailed Usage

For complete JdbcTemplate, Builder API, BaseMapper, Annotation, and Mapper File usage, see the [ElasticSearch Usage Guide](./usage).

## Core Topics

- [DSL Queries](./usage#exec-command): `POST /index/_search { "query": ... }` style
- [_id Backfill](./usage#id-fill): `useGeneratedKeys` backfill of document `_id`
- [Paginated Queries](./usage#pagination): Page object + from/size

## Relationship to General Documentation

For general API usage, see [Core API](../../guides/overview). For ES command syntax, see [Driver Adapter Command List](../../drivers/elastic/commands).
