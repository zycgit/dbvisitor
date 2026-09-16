---
id: procedures
sidebar_position: 11
title: Stored procedures
---

## Passing and receiving parameters {#parameters}

Use IN/OUT/INOUT parameters for scalar values. For example, create a procedure that adds two numbers:

```sql
CREATE PROCEDURE add_numbers @a INT, @b INT, @result INT OUTPUT AS
SET @result = @a + @b;
```

Send the complete definition as one statement. Then bind the input values and the output parameter:

```java
Map<String, Object> result = jdbc.call(
        "{call add_numbers(?, ?, ?)}",
        new Object[] { 10, 5, SqlArg.asInOut("result", 0, Types.INTEGER) });
Integer sum = (Integer) result.get("result"); // 15
```

Named parameters, parameter type declarations, and multiple scalar outputs are also supported. JDBC REF_CURSOR output mapping is not supported on this path. To receive rows returned by a procedure, use [result-set rules](../../guides/rules/result_rule.mdx#result-set), not a REF_CURSOR parameter.
