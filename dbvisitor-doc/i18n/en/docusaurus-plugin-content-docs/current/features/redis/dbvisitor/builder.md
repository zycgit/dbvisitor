---
id: builder
slug: /features/redis/builder
sidebar_position: 30
title: Builder API
---

## Support Scope {#support}

The Redis adapter has no builder dialect. Builder writes, write conflicts, queries, Map query mode, pagination, predicates, predicate values, grouping, and ordering are therefore unsupported.

This is a boundary of the Builder API, not of Redis command execution through dbVisitor. Use JdbcTemplate or Mapper to pass Redis commands:

```java
JdbcTemplate jdbc = new JdbcTemplate(dataSource);
jdbc.executeUpdate("SET ? ?", "user:1001:name", "Alice");
String name = jdbc.queryForObject("GET ?", new Object[] {"user:1001:name"}, String.class);
```

The `?` markers still bind values, and results can be mapped to objects. This does not translate builder `eq` or `in` calls into Redis commands.

## Choosing an API {#alternatives}

- Read and write: use commands such as `GET/SET` and `HGETALL/HSET`. See [Query Operations](query.mdx) and [Data Writes](write.mdx).
- Traverse data: use the `SCAN` cursor, not a builder page number. See [Pagination](pagination.mdx).
- Map objects: read command results into Java objects. See [Product Cache](../scenarios/product-cache.md).
