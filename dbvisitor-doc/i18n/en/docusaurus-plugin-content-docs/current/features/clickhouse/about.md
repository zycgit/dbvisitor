---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: ClickHouse
description: ClickHouse dialect capabilities, batch writes, primary key backfill, and usage differences in dbVisitor.
---

# ClickHouse

ClickHouse is an analytical database. Its table engines, partition keys, sort keys, and write model differ from traditional OLTP databases. dbVisitor provides a ClickHouse dialect for regular queries, pagination, and INSERT.

## Quick Overview of Differences

| Concern | ClickHouse Behavior |
|--------|----------------|
| Primary Key Generation | Typically generated application-side; does not rely on database auto-increment |
| Pagination | `LIMIT ?, ?` (offset, count) |
| Write Conflicts | Ignore/Update strategies not supported |
| Batch Writes | JDBC batch supported (when no backfill) |
| Stored Procedures | Not supported |
| Sequences | Not supported |

## Primary Key Generation

ClickHouse does not use the database auto-increment primary key model. Business identifiers are typically generated application-side (UUID, Snowflake ID, etc.) or via ClickHouse expressions. Relying on JDBC generated keys is not recommended.

## Object Mapping

```java
@Table("user_info")
public class UserInfo {
    @Column(value = "id", primary = true, keyType = KeyType.UUID32)
    private String id;
    @Column("name")
    private String name;
}
```

Use `KeyType.UUID32`/`KeyType.UUID36` to generate primary keys application-side.

## Batch Write Recommendations

- JDBC batch can be used to improve performance when no primary key backfill is needed
- ClickHouse recommends batch writes over row-by-row inserts
- With async_insert=1 and wait_for_async_insert=0, acknowledgement does not mean data is already visible; the default synchronous write path differs

## Special Topics

- [Primary Keys and Return Keys](./generated-keys): Recommended approach for application-side ID generation in ClickHouse, and boundaries with JDBC generated keys.
- [Dialect Details](./dialect-details): Low-level implementation of pagination, write strategy limitations, and batch behavior.

## Relationship to General Documentation

For general API usage, see [Core API](../../guides/overview). The following content supplements specific differences and recommended practices for ClickHouse.
