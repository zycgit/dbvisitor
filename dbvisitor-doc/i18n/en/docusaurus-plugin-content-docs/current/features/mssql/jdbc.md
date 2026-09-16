---
id: jdbc
slug: /features/mssql/programmatic
sidebar_position: 10
title: Programmatic API
---

## Stored Procedures and Functions {#routines}

Procedure input and output parameters, scalar function queries, and table-valued function queries are supported. Mapping a cursor through a JDBC REF_CURSOR OUT parameter is not supported; have the procedure return records through a SELECT result set instead.

See [Stored Procedure Calls](procedures.md#parameters) for parameter configuration and examples.
