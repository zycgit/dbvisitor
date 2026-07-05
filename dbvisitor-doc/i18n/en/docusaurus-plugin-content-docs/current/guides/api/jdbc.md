---
id: jdbc
sidebar_position: 2
hide_table_of_contents: true
title: 4.2 Programmatic API
description: The Programmatic API uses JdbcTemplate to execute SQL directly — ideal for scenarios requiring full control over SQL.
---

# 4.2 Programmatic API

The Programmatic API is based on [JdbcTemplate](../core/jdbc/about) and accesses the database with **SQL strings** as the primary interface. It is the lowest-level, most flexible relational database entry point in dbVisitor.

## Best For

- You already have clear SQL and want lightweight execution.
- SQL is complex — e.g. JOINs, subqueries, window functions, database-specific syntax.
- You need direct control over batch execution, stored procedures, callbacks, or connection-level operations.
- You do not want to set up object mapping for the current operation.

## Not Best For

- Lots of single-table CRUD with repetitive SQL — consider [Mapper API (BaseMapper)](./mapper) first.
- You want DAO organized as interfaces — consider [Mapper API](./mapper) first.
- You want to generate SQL with chainable conditions — consider [Fluent API](./lambda) first.

## Minimal Example

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
