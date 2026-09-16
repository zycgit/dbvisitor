---
id: jdbc
slug: /features/clickhouse/programmatic
sidebar_position: 10
title: Programmatic API
---

## Batch Operations {#batch}

Multiple writes and command error handling are supported. MergeTree does not enforce primary-key uniqueness, so duplicate IDs do not cause a primary-key conflict error. Do not use batch insert errors to detect duplicates; see [Duplicate Record Handling](conflict.mdx).

A completed batch call does not mean asynchronous mutations have finished. For read-after-write access, see [Waiting for Data Changes](write.mdx#wait-for-writes).

## Multiple Results {#multiple-results}

The JDBC driver does not support collecting results from multiple commands with `multipleExecute`, or the CallableStatement used by `call`. Execute queries separately with methods such as `queryForObject` and `queryForList`.

## Stored Procedures and Functions {#routines}

Scalar functions in SELECT and table functions in FROM are supported. Stored procedure calls, CallableStatement callbacks, and records returned through OUT parameters are not supported. See [Function Queries](functions.md#functions) for positional parameters, named parameters, and examples.
