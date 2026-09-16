---
id: jdbc
slug: /features/redis/programmatic
sidebar_position: 10
title: Programmatic API
---

## Updates {#updates}

`executeUpdate` runs commands such as `SET`, `HSET`, and `DEL`. `SET` overwrites the existing value. Use query methods for commands such as `INCR` that return data; do not choose the API solely by whether a command changes data. See [Data Writes](write.mdx#exec-command) for examples.

## Queries {#queries}

Query text is a Redis command, not SELECT. Read a scalar with `queryForObject("GET ?", key, String.class)`. When mapping Hash fields to a bean or Map, use the driver's result columns; see [Query Operations](query.mdx).

## Batch Operations {#batch}

`executeBatch` can execute several parameterized commands, but does not make them atomic. Invalid commands throw exceptions; `SET` on an existing key overwrites its value instead of causing a duplicate-key error. Use `MSET` to write several keys in one command; see [Multi-key Writes and Transactions](write.mdx#transactions).

## Stored Procedures and Functions {#routines}

Use `EVAL` for Lua calculations, with parameter binding and scalar return values. CallableStatement callbacks are also supported. SQL stored procedures, OUT-parameter records, and SQL table functions are not supported. See [Lua Calculations](query.mdx#functions) for examples.
