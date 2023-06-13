---
sidebar_position: 1
title: 如何使用？
description: dbVisitor ORM 工具的 SQL 模式主要围绕 JdbcTemplate 工具类展开，共提供了大量工具方法。
---

# 如何使用？

SQL 模式主要围绕 `JdbcTemplate` 工具类展开，共提供了大量工具方法。

:::tip
dbVisitor 的 `JdbcTemplate` 简单来说功能上是 Spring `JdbcTemplate`、`NamedParameterJdbcTemplate` 两者的合集。
:::

- 在使用 `queryForObject(String,Class<?>)` 和 `queryForList(String,Class<?>)` 系列方法时，Class 表示的实体类型将会遵循 **[对象映射](../objects/class-as-table.md)**。而 Spring JDBC 没有这方面能力。
- 新增了一组 `loadSQL`、`loadSplitSQL` 方法可以执行本地资源。
- 新增 `multipleExecute` 系列方法用于处理 `多语句` 或 `多返回值`

使用 dbVisitor 非常简单只需要 `new` 出来就可以了

```java
JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
```

或者

```java
JdbcTemplate jdbcTemplate = new JdbcTemplate(connection);
```
