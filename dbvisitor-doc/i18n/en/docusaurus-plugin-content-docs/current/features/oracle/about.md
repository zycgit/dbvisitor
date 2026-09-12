---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: Oracle
description: Oracle dialect capabilities, primary key backfill, IDENTITY, sequences, and MERGE conflict strategy in dbVisitor.
---

# Oracle

Oracle supports JdbcTemplate, annotation/XML Mapper, Lambda, BaseMapper, object mapping and JDBC transactions. Handwritten SQL retains Oracle semantics. Builders generate SQL through the Oracle dialect, but not every general interface applies.

## API Support and Boundaries

| Capability | Boundary |
| --- | --- |
| CRUD and pagination | Supported; builder pagination generates nested ROWNUM queries, with slicing performed by the database |
| Entity and Map mapping | Supported; unquoted column names commonly return uppercase, so do not assume raw Map keys are lowercase |
| Strings | Oracle treats empty character strings as NULL; empty strings and null need not round-trip distinctly |
| Generated keys | Auto uses generated keys; Sequence with @KeySeq obtains the value before insertion |
| Batch inserts | Lambda/BaseMapper use individual execution, including inserts without key backfill; this does not mean Oracle JDBC lacks batching |
| Transactions | Supports commit and rollback; isolation levels and savepoint release depend on Oracle JDBC, so not every database transaction option is portable |
| Arrays, cursors and specialized types | Use the corresponding Oracle JDBC registration, retrieval, and type-handling conventions |

## Quick Overview of Differences

| Concern | Oracle Behavior |
|--------|------------|
| Primary Key Generation | `IDENTITY` (12c+) or sequence |
| Pagination | `ROWNUM` nested query |
| Write Conflicts | `MERGE INTO ... WHEN MATCHED ... WHEN NOT MATCHED ...` |
| Batch Writes | Lambda/BaseMapper inserts execute individually; native JDBC Batch is a separate call path |
| Stored Procedures | Supported |
| Sequences | Supports `seq.NEXTVAL` |

## Key Generation

Oracle 12c+ `IDENTITY` columns are backfilled via JDBC generated keys. **Critical configuration**: `keyColumn` must be explicitly specified, otherwise the Oracle JDBC driver only returns `ROWID` instead of the business primary key column.

Mapper files can use `selectKey` to obtain a sequence value before INSERT:

```xml
<insert id="insertUser">
    <selectKey keyProperty="id" keyColumn="id" order="BEFORE">
        SELECT user_info_seq.NEXTVAL AS id FROM dual
    </selectKey>
    INSERT INTO user_info (id, name, age) VALUES (#{id}, #{name}, #{age})
</insert>
```

The Fluent API supports `KeyType.Sequence` + `@KeySeq` for automatic sequence assignment. See [Key Generation](./generated-keys.mdx).

## Special Topics

- [Data Backfill](./backfill.mdx): return inserted, updated, or deleted field values with `RETURNING INTO`.
- [Key Generation](./generated-keys): `IDENTITY`, sequences, `keyColumn`, and `selectKey`.
- [Pagination](./pagination.mdx): Pagination principles, usage and notes.
- [Multiple-Write Consistency](./write.mdx): handling partial failures with transactions.
- [Insert Conflicts](./conflict.mdx): Conflict scenarios, strategies, usage and notes.

## Related Documentation

For common usage, see [Core API](../../guides/overview).

- [Type Support](./types.md): Java values, storage choices and readback boundaries.
