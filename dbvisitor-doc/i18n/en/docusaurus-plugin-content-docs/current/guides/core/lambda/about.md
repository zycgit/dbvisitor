---
id: about
sidebar_position: 1
title: 5.3 Fluent API
description: Generate SQL programmatically, abstracting database dialect differences, with entity mode and Map Query Mode support.
---

# 5.3 Fluent API

`LambdaTemplate` is the core class of dbVisitor's Fluent API, building SQL statements programmatically and **automatically handling database dialect differences**.

## Choose a Mode First

| Mode | Requires object mapping | Best for |
| --- | --- | --- |
| Entity mode | Yes | Entity classes and method references such as `User::getName`. |
| [Mapped Map Mode](../map_query/mapped) | Yes | Table mapping exists, but row data is carried by `Map`. |
| [Freedom Map Mode](../map_query/freedom) | No | Direct table names, column names, and Map values. |

Entity mode and [Mapped Map Mode](../map_query/mapped) require [Object Mapping](../mapping/about). [Freedom Map Mode](../map_query/freedom) does not need entity mapping.

```java title='Entity class mapping'
@Table("users")
public class User {
    @Column(value = "id", primary = true, keyType = KeyType.Auto)
    private Long id;
    @Column("name")
    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

```java title='Creation and usage'
LambdaTemplate lambda = new LambdaTemplate(dataSource);

// Query example
List<User> users = lambda.query(User.class)
        .eq(User::getName, "alice")
        .queryForList();
```

:::tip[Hint]
How to obtain a LambdaTemplate depends on your project architecture. See **[Framework Integration](../../yourproject/buildtools#integration)** for details.
:::

## Principle {#principle}

Build operations through the `insert`, `update`, `delete`, `query`, and `freedom` series of methods. Developers describe SQL logic programmatically; at runtime, dialect-specific SQL is generated automatically and executed via JdbcTemplate.

## User Guide {#guide}

- [Insert](./insert), write data, batch inserts, conflict strategies (Ignore/Update).
- [Update](./update), three update approaches and unsafe update protection.
- [Delete](./delete), delete data.
- [Query](./query), query data.
- [Where Builder](./where_builder), build complex query conditions for Update/Delete/Query.
- [Group By](./group_by), GROUP BY grouped queries.
- [Order By](./order_by), ORDER BY query sorting.
- [Mapped Map Mode](../map_query/mapped), reuse object mapping while using Map as the row carrier.
- [Freedom Map Mode](../map_query/freedom), use table and column names directly without object mapping.
