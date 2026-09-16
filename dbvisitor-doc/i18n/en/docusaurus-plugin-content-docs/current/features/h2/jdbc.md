---
id: jdbc
slug: /features/h2/programmatic
sidebar_position: 10
title: Programmatic API
---

## Multiple Results {#multiple-results}

`call` can collect the result set from a single query. `multipleExecute` cannot collect all results from semicolon-separated SELECT statements. Execute the queries separately when you need several query results.

## Stored Procedures and Functions {#routines}

H2 registers Java methods with `CREATE ALIAS`. Use `SELECT function(?)` for a scalar return value or `SELECT * FROM function(?)` for a ResultSet, then receive the values with JdbcTemplate query methods.

SQL stored procedure calls with IN / OUT / INOUT parameters are not supported. To return several fields, have the function return a ResultSet instead of configuring stored procedure OUT parameters.
