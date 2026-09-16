---
id: jdbc
slug: /features/db2/programmatic
sidebar_position: 10
title: Programmatic API
---

## Multiple Results {#multiple-results}

`call` can collect a result set returned by a stored procedure through a `WITH RETURN TO CLIENT` cursor. `multipleExecute` cannot collect results from semicolon-separated SELECT statements in one call; execute independent queries separately.

## Stored Procedures and Functions {#routines}

Scalar IN / OUT / INOUT procedure parameters, scalar function queries, and table function queries are supported. JDBC REF_CURSOR OUT parameter mapping is not supported; receive records from the result set returned directly by the procedure instead.

See [Stored Procedure Calls](procedures.md#parameters) for parameter configuration and examples.
