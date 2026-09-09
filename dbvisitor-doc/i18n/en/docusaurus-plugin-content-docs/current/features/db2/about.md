---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: DB2
description: DB2 dialect capabilities, primary key backfill, IDENTITY, sequences, and MERGE conflict strategy in dbVisitor.
---

# DB2

DB2 can use all of dbVisitor's general capabilities including JDBC, Mapper, Lambda, BaseMapper, transactions, pagination, sequences, and object mapping.

## Quick Overview of Differences

| Concern | DB2 Behavior |
|--------|---------|
| Primary Key Generation | `IDENTITY` columns, backfilled via JDBC generated keys |
| Pagination | `ROWNUMBER() OVER(...)` with an outer range filter |
| Write Conflicts | `MERGE INTO ... WHEN MATCHED ... WHEN NOT MATCHED ...` |
| Batch Writes | Supported, but batch generated keys have driver limitations |
| Stored Procedures | Supported |
| Sequences | Supports `VALUES NEXT VALUE FOR seq` |

## Primary Key Backfill

DB2 `IDENTITY` columns are backfilled via JDBC generated keys. **Note**: DB2's batch generated keys have driver and configuration limitations; under certain configurations they cannot be used for batch updates. Therefore, dbVisitor conservatively executes row by row when primary key backfill is needed.

When no backfill is required, regular JDBC batch can be used.

## Write Conflict Strategies

| Strategy | DB2 Implementation |
|------|---------|
| Ignore | `MERGE INTO ... WHEN NOT MATCHED THEN INSERT` (requires PK) |
| Update | `MERGE INTO ... WHEN MATCHED THEN UPDATE ... WHEN NOT MATCHED THEN INSERT` (requires PK) |

## Sequences

DB2 dialect implements sequence support. Use `VALUES NEXT VALUE FOR seq` to read sequence values. In object mapping, use `KeyType.Sequence` + `@KeySeq`.

## Special Topics

- [Auto-generated Key Backfill](./generated-keys): `IDENTITY`, sequence, DB2 generated keys, and batch limitations.
- [Dialect Details](./dialect-details): Low-level implementation of ROWNUMBER pagination, MERGE syntax, sequences, and LIKE.

## Relationship to General Documentation

For general API usage, see [Core API](../../guides/overview). The following content supplements specific differences and recommended practices for DB2.
