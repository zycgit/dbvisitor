---
id: lambda
sidebar_position: 5
hide_table_of_contents: true
title: 4.5 Fluent API
description: The Fluent API uses LambdaTemplate to generate SQL through chainable methods and handles database dialect differences.
---

# 4.5 Fluent API

The Fluent API is based on [LambdaTemplate](../core/lambda/about). It uses chainable methods to describe INSERT, UPDATE, DELETE, QUERY and other operations, with dbVisitor generating the SQL.

## Best For

- You want to write little or no SQL.
- Query conditions have many combinations and you don't want to manually concatenate WHERE clauses.
- You want the same API to adapt across different database dialects.
- You need to express more complex conditions beyond BaseMapper's pre-built CRUD.

## Not Best For

- SQL is already fixed and very complex — writing SQL directly is clearer.
- You need lots of database-specific syntax, window functions, or complex JOINs.
- You want SQL fully maintained by DBAs or in standalone XML files.

For those scenarios, use [JdbcTemplate](./jdbc) or [File Mapper](./file_mapper).

## Three Modes

| Mode | Requires object mapping | Best for |
| --- | --- | --- |
| Entity mode | Yes | Entity classes exist; use method references like `User::getName` to reference columns. |
| Map mode | Yes | Table mapping exists, but the data carrier is `Map`. |
| Freedom mode | No | No entity mapping; directly uses table names and string column names. |

## Minimal Example

```java title='With DataSource'
DataSource dataSource = ...;
LambdaTemplate lambda = new LambdaTemplate(dataSource);

List<User> users = lambda.query(User.class)
        .eq(User::getStatus, "ACTIVE")
        .ge(User::getAge, 18)
        .queryForList();
```

```java title='With Connection'
Connection conn = ...;
LambdaTemplate lambda = new LambdaTemplate(conn);

List<User> users = lambda.query(User.class)
        .eq(User::getStatus, "ACTIVE")
        .ge(User::getAge, 18)
        .queryForList();
```

```java title='Freedom mode'
Map<String, Object> row = lambda.queryFreedom("users")
        .eq("id", 1)
        .queryForObject();
```

## Learn More

- [LambdaTemplate](../core/lambda/about): complete guide to the Fluent API.
- [Condition Builder](../core/lambda/where_builder): WHERE condition construction capabilities.
- [Object Mapping](../core/mapping/about): table and column mapping that Entity mode depends on.
