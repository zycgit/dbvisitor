---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: Dameng
description: Dameng database dialect capabilities, primary key backfill, IDENTITY, sequences, and conflict strategies in dbVisitor.
---

# Dameng

Dameng database can use dbVisitor's JDBC, Mapper, Lambda, BaseMapper, transactions, and regular object mapping capabilities.

## Quick Overview of Differences

| Concern | Dameng Behavior |
|--------|---------|
| Primary Key Generation | Auto-increment columns, backfilled via JDBC generated keys |
| Pagination | `LIMIT ?, ?` (offset, count) |
| Write Conflicts | `IGNORE_ROW_ON_DUPKEY_INDEX` (ignore) / `MERGE` (update) |
| Batch Writes | JDBC batch supported (when no backfill) |
| Stored Procedures | Supported |
| Sequences | Supports `SELECT seq.NEXTVAL` |

## Primary Key Backfill

Auto-increment columns are backfilled via JDBC generated keys. When no backfill is required, JDBC batch is preferred; when backfill is needed, conservative row-by-row execution is used.

## Write Conflict Strategies

Dameng's conflict strategy implementation is quite unique:

| Strategy | Dameng Implementation |
|------|---------|
| Ignore | `INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX */ INTO ...` (requires PK) |
| Update | `MERGE INTO ... WHEN MATCHED THEN UPDATE ... WHEN NOT MATCHED THEN INSERT ...` (requires PK and at least one non-PK column updatable) |

## Sequences

Dameng dialect implements sequence support. Use `SELECT seq.NEXTVAL`. In object mapping, use `KeyType.Sequence` + `@KeySeq`.

## Special Topics

- [Auto-generated Key Backfill](./generated-keys): Auto-increment columns, sequence, `keyColumn`, `selectKey`, and conflict strategy boundaries.
- [Dialect Details](./dialect-details): Low-level implementation of IGNORE_ROW_ON_DUPKEY_INDEX HINT, MERGE syntax, and sequences.

## Relationship to General Documentation

For general API usage, see [Core API](../../guides/overview). The following content supplements specific differences and recommended practices for Dameng.
