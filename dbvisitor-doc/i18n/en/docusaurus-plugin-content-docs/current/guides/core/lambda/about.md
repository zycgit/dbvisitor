---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: LambdaTemplate Class
description: Understand the preparations and necessary concepts for using LambdaTemplate to access the database.
---

## Preparation

Before using the LambdaTemplate class, you need to prepare a data source for it just like [JdbcTemplate](../jdbc/about). You can also pass JdbcTemplate as a parameter to the constructor of LambdaTemplate so that they share the same database connection.

- When using LambdaTemplate, you also need to establish object mapping for the tables in the database.
- Using annotation configuration mapping relationship is just one of the ways. [Click here](../mapping/about) to learn more detailed object mapping content.

```java title='1. Create object mapping for users table using annotation'
@Table("users")
public class User {
    @Column(name = "id", primary = true, keyType = KeyTypeEnum.Auto)
    private Long id;
    @Column("name")
    private String name;
    ...
}
```

- The `@Table` annotation is used to declare a type as an entity and specify the database table name corresponding to this entity.
- The `@Column` annotation is used to declare the mapping relationship between fields and columns of the type.
- In the above example, the `id` field is marked as a primary key, indicating that this column has database auto-increment characteristics.

```java title='2. Create Constructor'
DataSource dataSource = ...
LambdaTemplate lambda = new LambdaTemplate(dataSource);

// OR

Connection conn = ...
LambdaTemplate lambda = new LambdaTemplate(conn);
```

- Database reading and writing can be achieved through various methods provided by the lambda object.
- By default, LambdaTemplate uses `RegistryManager.DEFAULT` registry to manage type mapping information for it.

In actual usage, the way to obtain LambdaTemplate may vary depending on your project architecture. The above code demonstrates a primitive way to create LambdaTemplate.
You can choose the appropriate way to obtain LambdaTemplate according to your project architecture. For details, please refer to: **[Framework Integration](../../yourproject/buildtools#integration)**

Relevant Classes
- net.hasor.dbvisitor.jdbc.core.JdbcTemplate
- net.hasor.dbvisitor.jdbc.JdbcOperations
- net.hasor.dbvisitor.lambda.LambdaTemplate
- net.hasor.dbvisitor.lambda.LambdaOperations
- net.hasor.dbvisitor.mapping.Table
- net.hasor.dbvisitor.mapping.Column

## Principle {#principle}

The core implementation principle is to provide operations on tables through the `insert`, `update`, `delete`, `query`, and `freedom` series of methods provided by the LambdaTemplate class.

- `insert` is used to generate and execute INSERT statements.
- `update` is used to generate and execute UPDATE statements.
- `delete` is used to generate and execute DELETE statements.
- `query` is used to generate and execute SELECT statements.

Developers indicate the generation logic of SQL statements programmatically. When executed, SQL statements and parameters are generated according to the logic, and finally passed to JdbcTemplate for execution and obtaining return values.

## User Guide {#guide}

- [Insert](./insert), learn how to write data, how to use batch writing to improve efficiency, and handle data conflicts during writing.
- [Update](./update), learn three methods of updating data and how to handle some unsafe update operations.
- [Delete](./delete), learn how to delete data using LambdaTemplate.
- [Query](./query), learn how to use the query methods provided by dbVisitor's constructor for data query.
- [Condition Constructor](./where-builder), by using condition constructor, rich query conditions can be built and used in Update, Delete, and Query operations.
- [Grouping](./groupby), perform grouping queries.
- [Sorting](./orderby), perform query sorting.
- [API designed for Map structure](./for-map), although there are entity objects to carry database table mappings, sometimes the Map structure is still a good data carrier.
- [Freedom Mode](./freedom), in freedom mode based on Map structure, SQL statements can be generated and executed without object mapping classes.
