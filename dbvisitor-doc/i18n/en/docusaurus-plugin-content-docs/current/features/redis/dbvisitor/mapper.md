---
id: mapper
slug: /features/redis/mapper
sidebar_position: 20
title: Mapper API
---

## Method Annotations {#annotations}

`@Query`, `@Insert`, `@Update`, `@Delete`, and `@Execute` accept Redis commands directly; SQL is not required.

```java
@SimpleMapper
public interface ValueMapper {
    @Insert("SET #{key} #{value}")
    int put(@Param("key") String key, @Param("value") String value);

    @Query("GET #{key}")
    String get(@Param("key") String key);
}
```

Invalid commands raise errors. GET on a missing key returns null, while SET overwrites an existing key; these are not missing-table or duplicate-key errors. See [Query Operations](query.mdx) and [Data Writes](write.mdx).

## Mapper Reads and Writes {#operations}

Redis has no automatic BaseMapper CRUD dialect. It cannot generate Redis commands from entity calls such as `insert`, `selectById`, or `update`. Use method annotations or file mappers instead, including for sample queries and writes using Map arguments.

Redis results can still be mapped to objects; see [Product Cache](../scenarios/product-cache.md).

## Key Strategies {#keys}

The application supplies key names. Business IDs can be obtained before writing with `selectKey` and INCR, or retrieved from a Lua script result. Redis has no JDBC generated keys or database composite primary keys; auto-increment column settings do not apply. See [Keys and IDs](generated-keys.mdx).

## Pagination {#pagination}

Automatic SQL pagination is unavailable: a Page argument cannot rewrite a Redis command into LIMIT / OFFSET. Use SCAN cursors to iterate keys, or LRANGE / ZRANGE to read ranges. See [Pagination](pagination.mdx).

## Execution Options {#options}

Use the default or `FORWARD_ONLY` result-set type; `SCROLL_INSENSITIVE` and `SCROLL_SENSITIVE` are unsupported. Re-execute the command or collect results into a List for repeated reading.

Result-handler interfaces and execution options are separate capabilities. See [Receiving Results](result-handling.md).

## Calling Builders {#builder}

A builder object can be obtained, but Redis has no dialect for executing builder CRUD. Do not use `mapper.lambda()` or `session.lambda()` to generate writes and queries; use method annotations or native commands through `session.jdbc()`.

## Referencing File Mappers {#file-mapper}

Use `@RefMapper` or a statement ID to query and write with Redis commands in a mapper file. BaseMapper's file-statement calls remain usable even though its automatic CRUD is unavailable.

Write Redis commands in the file, not SQL. See [Data Writes](write.mdx#base-mapper).

## Session Management {#session}

Session can create native-command mappers, load file mappers, and expose Redis through `jdbc()`. These APIs can read each other's keys without automatic BaseMapper CRUD.

Unavailable builders or automatic CRUD do not make Session unavailable. Shared access also does not imply JDBC transaction rollback for Redis writes; see [Transaction Support](transactions.md).
