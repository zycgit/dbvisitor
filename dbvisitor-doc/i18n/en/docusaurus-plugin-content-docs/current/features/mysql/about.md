---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: MySQL
description: MySQL dialect capabilities, type mappings, AUTO_INCREMENT, pagination, duplicate key strategies, and stored procedure differences in dbVisitor.
---

# MySQL

MySQL can use all of dbVisitor's general capabilities including JDBC, Mapper, Lambda, BaseMapper, transactions, and object mapping.

## Quick Overview of Differences

| Concern | MySQL Behavior |
|--------|----------|
| Primary Key Generation | `AUTO_INCREMENT`, backfilled via JDBC generated keys |
| Pagination | `LIMIT ?, ?` (offset, count) |
| Write Conflicts | `INSERT IGNORE` (ignore) / `ON DUPLICATE KEY UPDATE` (update) |
| Batch Writes | JDBC batch supported |
| Stored Procedures | Supported |
| Sequences | Not supported (use AUTO_INCREMENT instead) |

## Primary Key Backfill

`AUTO_INCREMENT` primary keys are backfilled via JDBC generated keys. Lambda / BaseMapper selects the execution method based on the following strategy:

- No backfill columns: regular JDBC batch
- `Into` + with backfill columns: JDBC batch generated keys
- `Ignore` / `Update` + with backfill columns: conservative row-by-row execution

Duplicate key strategies are triggered by database unique keys or primary keys, not dependent on the passed primary key column list.

## Write Conflict Strategies

| Strategy | MySQL Implementation |
|------|-----------|
| Ignore | `INSERT IGNORE INTO ...` |
| Update | `INSERT INTO ... ON DUPLICATE KEY UPDATE ...` |

## Type Mapping Highlights

- `TINYINT(1)` maps to Java `Boolean`/`boolean`
- `DATETIME` / `TIMESTAMP` maps to `java.time.LocalDateTime` or `java.util.Date`
- `JSON` type serialized via `JsonTypeHandler`
- For enums, recommend using `VARCHAR` with `EnumOfValue`/`EnumOfCode`

## Special Topics

- [Auto-generated Key Backfill](./generated-keys): Detailed configuration for `AUTO_INCREMENT`, JDBC generated keys, batch backfill, and duplicate key strategies.
- [Dialect Details](./dialect-details): Pagination SQL, identifier quoting, LIKE syntax, conflict strategy implementation details.

## Relationship to General Documentation

For general API usage, see [Core API](../../guides/overview). The following content supplements specific differences and recommended practices for MySQL.
