---
id: multiple_results
sidebar_position: 6
title: Multiple Results
description: JdbcTemplate.multipleExecute lets you capture all results when SQL produces multiple result sets or values.
---

# Multiple Results

The **multipleExecute** series of methods in **JdbcTemplate** solves the problem of SQL statements producing multiple values that are hard to identify and retrieve.

## When It Happens
- Sending multiple **unsplit** SQL statements in a single request.
  ```sql title="Example: send the entire block to MySQL"
  set @userName = convert(? USING utf8);
  select * from test_user where name  = @userName;
  select * from test_user where name != @userName;
  ```
  :::info
  Some databases (e.g., MySQL) require enabling multi-statement support, such as `allowMultiQueries=true` in the JDBC URL.
  :::
- Stored procedures returning multiple result sets, intentionally or not.
  ```sql title="Example: MySQL procedure yielding two result sets"
  create procedure proc_multi_result(in userName varchar(200))
  begin
    select * from test_user where name  = userName;
    select * from test_user where name != userName;
  end;
  ```
- Other situations that produce multiple result sets.

## Usage

```java title='One argument, two result sets: matches vs mismatches'
String multipleSql = " set @userName = convert(? USING utf8);" +
                     " select * from test_user where name  = @userName;" +
                     " select * from test_user where name != @userName;";
Map<String, Object> result = jdbc.multipleExecute(multipleSql, "muhammad");
```

- See **[Arguments](../../args/about)** for all supported binding styles.

## Return Value

- By default, each result set is represented as **[List/Map](../../result/for_map)**. multipleExecute returns a Map whose values are update counts or result-set Lists; rows default to Maps.
- You can embed rules in SQL to control how each result set is handled.

```sql title="1. Annotate statements with @{resultSet} rules"
set @userName = convert(? USING utf8);           @{resultUpdate,name=upd}
select * from test_user where name  = @userName; @{resultSet,name=res1,javaType=net.demo.dto.User}
select * from test_user where name != @userName; @{resultSet,name=res2,javaType=net.demo.dto.User}
```

Explanation:
- The SET statement uses a rule to label the update count as `upd`.
- The first SELECT uses a rule to label its result set `res1` and map rows to `net.demo.dto.User`.
- The second SELECT uses a rule to label its result set `res2` and map rows to `net.demo.dto.User`.

```java title="2. Execute annotated SQL and fetch typed results"
String query = "set @userName = convert(? USING utf8);           @{resultUpdate,name=upd}" +
               "select * from test_user where name  = @userName; @{resultSet,name=res1,javaType=net.demo.dto.User}" +
               "select * from test_user where name != @userName; @{resultSet,name=res2,javaType=net.demo.dto.User}";
Map<String, Object> result = jdbcTemplate.multipleExecute(query, "muhammad");

List<User> result1 = (List<User>) result.get("res1"); // First SELECT
List<User> result2 = (List<User>) result.get("res2"); // Second SELECT
```

:::info
See **<span class="badge badge--warning">[RESULT rule](../../rules/result_rule#result-set)</span>** for more on `@{resultSet,...}`.
:::

:::info
- `multipleExecute` returns a `LinkedCaseInsensitiveMap` or `LinkedHashMap` to preserve statement order.
- See **[Map Case Sensitivity](./query#case)** for details.
:::
