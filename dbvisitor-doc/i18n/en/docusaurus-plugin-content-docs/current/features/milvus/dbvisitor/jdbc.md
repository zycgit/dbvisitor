---
id: jdbc
slug: /features/milvus/programmatic
sidebar_position: 10
title: Programmatic API
---

## Updates {#updates}

`executeUpdate` runs INSERT, UPDATE, and DELETE in Milvus SQL. Field updates use Partial Upsert; updates and deletes that first select records run in pages. Returned counts cannot always tell whether a record previously existed; see [Data Writes](write.mdx#write-behavior).

## Queries {#queries}

SELECT executes through Milvus query or search APIs. Receive results as beans, Maps, scalars, or lists. See [Query Operations](query.mdx) for vector ordering and field mapping.

## Batch Operations {#batch}

`executeBatch` executes commands one by one, without JDBC batch or an all-or-nothing rollback. Invalid commands report errors, but INSERT does not guarantee an error for duplicate primary keys. Do not rely on exceptions to detect duplicates; see [Insert Conflicts](write.mdx#insert-conflict).

## Stored Procedures and Functions {#routines}

SQL stored procedure, scalar function, and table function calls are not supported. Collection Functions such as BM25 compute fields; they are not stored routines invoked through `call`. See [Function Definitions](../ddl/functions.md).
