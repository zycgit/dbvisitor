---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: Oracle
description: Oracle dialect capabilities, primary key backfill, IDENTITY, sequences, and MERGE conflict strategy in dbVisitor.
---

# Oracle

Oracle can use all of dbVisitor's general capabilities including JDBC, Mapper, Lambda, BaseMapper, transactions, functions, procedures, and sequences.

## Quick Overview of Differences

| Concern | Oracle Behavior |
|--------|------------|
| Primary Key Generation | `IDENTITY` (12c+) or sequence; recommend `selectKey` to get value before INSERT |
| Pagination | `ROWNUM` nested query |
| Write Conflicts | `MERGE INTO ... WHEN MATCHED ... WHEN NOT MATCHED ...` |
| Batch Writes | Supported, but falls back to row-by-row when primary key backfill is needed |
| Stored Procedures | Supported |
| Sequences | Supports `seq.NEXTVAL` |

## Primary Key Backfill

Oracle 12c+ `IDENTITY` columns are backfilled via JDBC generated keys. **Critical configuration**: `keyColumn` must be explicitly specified, otherwise the Oracle JDBC driver only returns `ROWID` instead of the business primary key column.

Recommended approach: for sequence scenarios, use `selectKey` to get the sequence value before INSERT, then pass it into the INSERT:

```xml
<insert id="insertUser">
    <selectKey keyProperty="id" keyColumn="id" order="BEFORE">
        SELECT user_info_seq.NEXTVAL AS id FROM dual
    </selectKey>
    INSERT INTO user_info (id, name, age) VALUES (#{id}, #{name}, #{age})
</insert>
```

The current Oracle dialect does not implement `SeqSqlDialect`; do not use `KeyType.Sequence` + `@KeySeq` directly. Use `selectKey` above or read the sequence yourself.

## RETURNING INTO

Oracle's `RETURNING ... INTO` is an OUT parameter model, not equivalent to PostgreSQL `RETURNING`. **Do not** use `generatedKeySource="resultSet"` with Oracle `RETURNING INTO` in Mapper XML.

## Write Conflict Strategies

| Strategy | Oracle Implementation |
|------|------------|
| Ignore | `MERGE INTO ... WHEN NOT MATCHED THEN INSERT` (requires PK) |
| Update | `MERGE INTO ... WHEN MATCHED THEN UPDATE ... WHEN NOT MATCHED THEN INSERT` (requires PK) |

## Special Topics

- [Auto-generated Key Backfill](./generated-keys): Detailed configuration for `IDENTITY`, sequence, `keyColumn`, `selectKey`, and `RETURNING INTO`.
- [Dialect Details](./dialect-details): Low-level implementation of ROWNUM pagination, MERGE syntax, and all-OneByOne strategy.

## Relationship to General Documentation

For general API usage, see [Core API](../../guides/overview). The following content supplements specific differences and recommended practices for Oracle.
