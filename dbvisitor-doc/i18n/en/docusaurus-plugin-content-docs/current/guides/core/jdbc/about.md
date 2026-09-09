---
id: about
sidebar_position: 1
title: 5.1 Programmatic API
description: SQL string-based database operation wrapper with automatic connection management and exception handling.
---

# 5.1 Programmatic API

`JdbcTemplate` is a database operation wrapper designed by dbVisitor specifically for **SQL string** scenarios. It is stateless and can be created and destroyed at any time.

## Start by Scenario

| Goal | Entry |
| --- | --- |
| Run an existing SQL query | [Queries](./query) |
| Execute INSERT, UPDATE, DELETE, or DDL | [Updates](./update) |
| Submit many argument sets or many SQL statements | [Batches](./batch) |
| Call stored procedures or functions | [Stored Procedure Calls](./procedure) |
| SQL returns multiple result sets | [Multiple Results](./multiple_results) |
| Work directly with Connection or Statement | [Template Methods](./callback) |

```java title='Creation and Usage'
JdbcTemplate jdbc = new JdbcTemplate(dataSource);

// Query
List<Map<String, Object>> rows = jdbc.queryForList("select * from users where age > ?", 18);

// Update
int affected = jdbc.executeUpdate("update users set name = ? where id = ?", new Object[] { "alice", 1 });
```

:::tip[Hint]
How you obtain JdbcTemplate depends on your project architecture. See **[Framework Integration](../../yourproject/buildtools#integration)** for details.
:::

## Principle

JdbcTemplate is based on the Template pattern. Inside the template method, it automatically handles acquiring connections, releasing connections, and catching exceptions. Higher-level code only needs to focus on using the Connection.

```java title='Core Template Method'
T result = jdbc.execute((ConnectionCallback<T>) con -> {
   // Use Connection directly
});
```

## User Guide {#guide}

- [Query](./query), execute SELECT or other statements with return results.
- [Update](./update), execute INSERT, UPDATE, DELETE, or DDL.
- [Batch](./batch), execute batch operations.
- [Stored Procedure](./procedure), call stored procedures/functions.
- [Rules](../../rules/about), use rules to give SQL dynamic capabilities.
- [Multi-value](./multiple_results), execute SQL with multiple statements and obtain all results.
- [Script](./execute), execute SQL script files or multiple statements.
- [Using Template](./callback), operate the database directly via template methods.
- [Advanced Features](./options), JdbcTemplate-specific properties and features.
- [Arguments](../../args/about), learn about different ways to pass arguments.
- [Receiving Results](../../result/about), learn about different ways to receive results.
