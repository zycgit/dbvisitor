---
id: about
sidebar_position: 1
title: 5.5 Map Query Mode
description: Map Query Mode uses Map as the row data carrier, including mapped Map mode and mapping-free Freedom Map mode.
---

# 5.5 Map Query Mode

Map Query Mode uses `Map<String, Object>` as the row data carrier. It has two peer entrances: **Mapped Map Mode** reuses entity object mapping, and **Freedom Map Mode** uses table and column names directly.

## Suitable For

- Data comes from dynamic forms, imports, messages, or intermediate transformation results.
- Query, insert, update, and delete operations should use the same builder API with Map data.
- Object mapping already exists, but the application layer does not want entity objects.
- There is no entity class, or table and column names come from configuration or runtime rules.

## Not Suitable For

- Compile-time field checks are required. Use [LambdaTemplate Entity Mode](../lambda/about) and method references.
- Only one-row primary-key operations are needed. Start with [BaseMapper](../mapper/about#base-mapper).
- SQL shape is complex, with many joins, subqueries, or database-specific syntax. [JdbcTemplate](../jdbc/about) or [Mapper Files](../file/about) are usually clearer.

## Two Map Modes

| Mode | Entrance | Requires object mapping | Field interpretation | Details |
| --- | --- | --- | --- | --- |
| Mapped Map Mode | `lambda.query(User.class).asMap()` | Yes | Java property names, converted by mapping | [Mapped Map Mode](./mapped) |
| Freedom Map Mode | `lambda.queryFreedom("users")` | No | Table and column names come directly from strings | [Freedom Map Mode](./freedom) |

```text title='Choose a mode'
Entity mapping exists
        |
        +-- Need Map as row carrier
        |       |
        |       +-- Use asMap()
        |
        +-- Need entity objects and method references
                |
                +-- Use Entity mode

No entity mapping
        |
        +-- Caller provides table and column names
                |
                +-- Use *Freedom()
```

Both modes use the same `MapQuery`, `MapInsert`, `MapUpdate`, and `MapDelete` APIs. Shared capabilities include conditions, ordering, grouping, paging, insert, update, delete, and unsafe-update protection.

## Query And Write Entrances

| Operation | Mapped Map Mode | Freedom Map Mode |
| --- | --- | --- |
| Query | `lambda.query(User.class).asMap()` | `lambda.queryFreedom("users")` |
| Insert | `lambda.insert(User.class).asMap()` | `lambda.insertFreedom("users")` |
| Update | `lambda.update(User.class).asMap()` | `lambda.updateFreedom("users")` |
| Delete | `lambda.delete(User.class).asMap()` | `lambda.deleteFreedom("users")` |

See [Map Insert, Update, And Delete](./write) for operation details.

## Field Sources

```text title='Field interpretation'
Mapped Map Mode
Map key = Java property name
loginName -> object mapping -> login_name

Freedom Map Mode
Map key = caller-provided column name
login_name -> SQL identifier
```

Mapped Map Mode reads object mapping, so it can reuse column conversion, field filtering, TypeHandler, and write policies. Freedom Map Mode does not read entity mapping; the caller provides table and column names directly.

## Further Reading

- [Mapped Map Mode](./mapped), entity mapping exists, but Map is used as the row carrier.
- [Freedom Map Mode](./freedom), no entity class, table and column names are provided directly.
- [Map Insert, Update, And Delete](./write), write and mutation operations in both modes.
- [Object Mapping](../mapping/about), field filtering, column conversion, and type handling used by Mapped Map Mode.
