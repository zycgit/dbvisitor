---
id: lambda
sidebar_position: 5
hide_table_of_contents: true
title: 4.5 Builder API
description: The Builder API uses LambdaTemplate and dialects to generate SQL or target commands through shared chained methods for supported relational, document, search and vector data sources.
---

# 4.5 Builder API

The Builder API uses [LambdaTemplate](../core/lambda/about) to describe INSERT, UPDATE, DELETE, QUERY and other operations through chained methods. The matching dialect generates SQL or target commands for execution through a JDBC driver. This is one application adapter entry point for shared access patterns across data sources.

For example, the same condition builder pattern can produce SQL through a relational dialect, document commands through the MongoDB dialect, or Query DSL through an Elasticsearch dialect. Supported operations and conditions are listed in the [builder capabilities by data source](../../features/differences/builder.md).

## Best For

- You want to reduce handwritten SQL or data source-specific commands.
- Query conditions have many combinations and you don't want to manually concatenate WHERE clauses.
- You want the same API to adapt across different database dialects.
- You need to express more complex conditions beyond BaseMapper's pre-built CRUD.

## Not Best For

- SQL or DSL is already fixed and complex enough that maintaining the commands directly is clearer.
- You need lots of database-specific syntax, window functions, or complex JOINs.
- You want SQL/DSL fully maintained by DBAs or in standalone XML files.

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

- [LambdaTemplate](../core/lambda/about): complete guide to the Builder API.
- [Condition Builder](../core/lambda/where_builder): WHERE condition construction capabilities.
- [Object Mapping](../core/mapping/about): table and column mapping that Entity mode depends on.
