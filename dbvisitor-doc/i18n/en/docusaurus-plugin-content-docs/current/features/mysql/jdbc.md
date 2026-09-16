---
id: jdbc
slug: /features/mysql/programmatic
sidebar_position: 10
title: Programmatic API
---

## Stored Procedures and Functions {#routines}

- Stored procedures: scalar IN / OUT / INOUT parameters and directly returned result sets are supported. Do not use JDBC REF_CURSOR OUT parameters to receive cursors.
- Stored functions: return a single value, not a table or several fields through OUT parameters. Use a stored procedure for multi-field results.

See [Procedure Parameters](procedures.md#parameters) and [Reading Function Values](procedures.md#functions) for examples.
