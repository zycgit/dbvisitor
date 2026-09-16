---
id: procedures
sidebar_position: 11
title: Procedures and functions
---

## Passing and receiving parameters {#parameters}

Use IN/OUT/INOUT parameters for scalar values. For example, create a procedure that adds two numbers:

```sql
CREATE PROCEDURE add_numbers(IN a INT, IN b INT, INOUT result INT)
BEGIN SET result = a + b; END
```

Send the complete definition as one statement. Then bind the input values and the output parameter:

```java
Map<String, Object> result = jdbc.call(
        "CALL add_numbers(?, ?, ?)",
        new Object[] { 10, 5, SqlArg.asInOut("result", 0, Types.INTEGER) });
Integer sum = (Integer) result.get("result"); // 15
```

Named parameters, parameter type declarations, and multiple scalar outputs are also supported. JDBC REF_CURSOR output mapping is not supported on this path. To receive rows returned by a procedure, use [result-set rules](../../guides/rules/result_rule.mdx#result-set), not a REF_CURSOR parameter.

## Reading a function value {#functions}

A MySQL stored function returns one scalar value, not a table or an OUT-parameter record. Read it with a query method:

```sql
CREATE FUNCTION add_numbers(a INT, b INT)
RETURNS INT DETERMINISTIC RETURN a + b;
```

```java
Integer sum = jdbc.queryForObject("SELECT add_numbers(?, ?)",
        new Object[] { 10, 5 }, Integer.class);
```

Positional and named arguments, type conversion, and CallableStatement callbacks are supported. For multiple output fields or returned rows, use a procedure rather than declaring a table-returning function.
