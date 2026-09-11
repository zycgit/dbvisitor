---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: Redis Features
description: Redis data source capability scope, API support, and usage patterns in dbVisitor.
---

<span id="redis-data-source-features" />

# Redis Features

dbVisitor accesses Redis data sources via the [JDBC-Redis](../../drivers/redis/about) driver, based on the JDBC protocol.

## Quick Overview of Differences

| Concern | Redis Behavior |
|--------|-----------|
| API Support | JdbcTemplate, Annotations, Mapper File (Builder API and BaseMapper not supported) |
| Primary Key Generation | Not supported (Key-Value model, no primary key concept) |
| Pagination | Not supported |
| Batch Writes | executeBatch not supported |
| Stored Procedures | Not supported |
| Object Mapping | RowMapper/result mapping and JSON TypeHandler are supported; no entity-driven CRUD builder |

**Not supported:** Builder API, Generic Mapper, executeBatch, Stored Procedures

## Concept Analogy

Execution results of different Redis commands fall into two categories:
- **Update count** — analogous to INSERT/UPDATE/DELETE, obtained via `executeUpdate`
- **Single/Multi-row results** — analogous to SELECT result sets

## Supported Usage Modes

Redis supports three usage modes:
- [JdbcTemplate Command Mode](./usage#exec-command): Execute Redis commands directly
- [Annotation Mode](./usage#exec-annotation): Mapper interface + `@Insert`/`@Query`/`@Delete`
- [File Mode](./usage#exec-file): Mapper XML files

For complete usage and Redis data type operations (String, Hash, List, Set, Sorted Set), see the [Redis Usage Guide](./usage).

## Core Topics

- [Object Serialization](./usage#json-serialization): `@BindTypeHandler(JsonTypeHandler.class)` for reading/writing Java objects
- [Data Type Operations](./usage#redis-type): CRUD for String, Hash, List, Set, Sorted Set
- [Batch Operations](./usage#multi-key): MGET/MSET/DEL for multiple keys

## Relationship to General Documentation

For general API usage, see [Core API](../../guides/overview). For Redis command syntax, see [Driver Adapter Command List](./commands).

## Documentation

- [JDBC installation and connection](../../drivers/redis/connection.mdx)
- [JDBC operations](./jdbc.mdx)
- [Command reference](./commands.md)
- [dbVisitor API usage](./usage.mdx)
