---
id: jdbc
slug: /features/mongo/programmatic
sidebar_position: 10
title: Programmatic API
---

## Updates {#updates}

`executeUpdate` accepts MongoDB commands such as `db.collection.insertOne(...)`, `updateMany(...)`, and `deleteMany(...)`, not relational INSERT / UPDATE / DELETE statements. See [Data Writes](write.mdx#exec-command) for examples.

## Queries {#queries}

Run commands such as `find` and `aggregate`, then use JdbcTemplate to read beans, Maps, scalars, or lists. Fields come from the result documents; see [Query Operations](query.mdx) for mapping examples.

## Batch Operations {#batch}

Use `executeBatch` for multiple commands. Duplicate `_id` values and invalid commands report errors. Earlier writes may already have succeeded and are not automatically undone by a later failure; see [Multiple Writes and Transactions](write.mdx#transactions).

## Stored Procedures and Functions {#routines}

Use aggregation expressions for calculations and query methods to receive the results. SQL stored procedures, CallableStatement function callbacks, OUT-parameter records, and SQL table functions are not supported. See [Aggregation Calculations](query.mdx#functions) for examples.
