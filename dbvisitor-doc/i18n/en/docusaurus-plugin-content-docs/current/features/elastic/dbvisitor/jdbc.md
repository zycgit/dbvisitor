---
id: jdbc
slug: /features/elastic/programmatic
sidebar_position: 10
title: Programmatic API
---

## Updates {#updates}

`executeUpdate` accepts commands made up of an HTTP method, a path, and a request body, not relational INSERT / UPDATE / DELETE statements. See [Data Writes](write.mdx#exec-command) for document IDs, update conditions, and examples.

## Queries {#queries}

Query with `_search` and Query DSL. JdbcTemplate reads beans, Maps, scalars, or lists from the driver's result columns. Pre-read settings affect field expansion; see [Query Operations](query.mdx).

## Stored Procedures and Functions {#routines}

Use `script_fields` in `_search` to calculate values and read the results. SQL stored procedures, CallableStatement function callbacks, OUT-parameter records, and SQL table functions are not supported. See [Script Calculations](query.mdx#functions) for examples.
