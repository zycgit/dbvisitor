---
id: jdbc
sidebar_position: 2
hide_table_of_contents: true
title: 4.2 Programmatic API
description: The Programmatic API uses JdbcTemplate to execute SQL, DSL or native commands supported by the driver with shared parameter binding and result handling.
---

# 4.2 Programmatic API

The Programmatic API uses [JdbcTemplate](../core/jdbc/about) to execute **SQL, DSL or native commands** supported by the target driver through JDBC. It is the application adapter entry point for direct control over command text, with shared parameter binding, execution calls and result mapping.

The same `JdbcTemplate` API can execute relational SQL or, through the appropriate driver, MongoDB commands, Elasticsearch REST/Query DSL, Redis commands and Milvus SQL-style commands. Syntax and capabilities depend on the target data source and driver; consult [data source differences](../../features/overview.md) for specific usage.

## Best For

- You already have SQL, DSL or native commands and want shared parameter binding and result mapping.
- Queries are complex, such as relational JOINs, subqueries and window functions, or NoSQL-specific queries and aggregations.
- You need direct control over batch execution, stored procedures, callbacks, or connection-level operations.
- You do not want to set up object mapping for the current operation.

## Not Best For

- Lots of single-table CRUD with repetitive SQL — consider [Mapper API (BaseMapper)](./mapper) first.
- You want DAO organized as interfaces — consider [Mapper API](./mapper) first.
- You want to generate SQL or target commands with chained conditions; consider [Builder API](./lambda) where the dialect supports it.

## Minimal Example

The following relational SQL examples demonstrate parameter binding and result mapping.

```java title='With DataSource'
DataSource dataSource = ...;
JdbcTemplate jdbc = new JdbcTemplate(dataSource);

List<User> users = jdbc.queryForList(
        "select * from users where age >= :age",
        CollectionUtils.asMap("age", 18),
        User.class);
```

```java title='With Connection'
Connection conn = ...;
JdbcTemplate jdbc = new JdbcTemplate(conn);

List<User> users = jdbc.queryForList(
        "select * from users where age >= :age",
        CollectionUtils.asMap("age", 18),
        User.class);
```

## Learn More

- [JdbcTemplate](../core/jdbc/about): full capability entry point.
- [Parameter Passing](../args/about): positional parameters, named parameters, interface-based parameter passing.
- [Result Handling](../result/about): RowMapper, Map, ResultSetExtractor and other result processing approaches.
