---
id: functions
sidebar_position: 12
title: Function queries
---

## Scalar and table functions {#functions}

ClickHouse functions run through SELECT. SQL UDFs return scalar values; table functions appear in FROM and produce rows.

```sql
CREATE FUNCTION add_numbers AS (a, b) -> a + b;
```

```java
Integer sum = jdbc.queryForObject("SELECT add_numbers(?, ?)",
        new Object[] { 10, 5 }, Integer.class);
List<Map<String, Object>> rows = jdbc.queryForList("SELECT number FROM numbers(5)");
```

Both positional and named parameters are supported. Use JdbcTemplate query methods for the results. This path does not support JDBC CallableStatement callbacks or records returned through OUT parameters.
