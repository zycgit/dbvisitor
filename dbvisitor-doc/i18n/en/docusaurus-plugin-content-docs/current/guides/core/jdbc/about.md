---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: JdbcTemplate Class
description: Understand the preparations and necessary concepts for using JdbcTemplate to access the database.
---

## Preparation

JdbcTemplate is specifically designed by dbVisitor for usage scenarios based on SQL strings. The API encapsulates numerous utility methods that can cover most database operation needs.

- Before use, you need to prepare a data source connection, which can be `javax.sql.DataSource` or a concrete `java.sql.Connection`.
- JdbcTemplate is **stateless** and can be created and destroyed at any time.

Below is an example using `javax.sql.DataSource` with [HikariCP](https://github.com/brettwooldridge/HikariCP) as the data source.

```java title='1. Create Data Source'
HikariConfig config = new HikariConfig();
config.setAutoCommit(false);
config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/test");
config.setUsername("root");
config.setPassword("123456");
config.setDriverClassName("com.mysql.jdbc.Driver");
config.setMaximumPoolSize(20);
config.setMinimumIdle(1);
config.setConnectionTimeout(30000);

DataSource dbPool =  new HikariDataSource(config);
```

Next, define JdbcTemplate and pass the created dbPool data source as a dependency parameter to the JdbcTemplate constructor.

```java title='2. Create JdbcTemplate'
JdbcTemplate jdbc = new JdbcTemplate(dbPool);
```

In actual usage, the way to obtain JdbcTemplate may vary depending on your project architecture. The above code demonstrates a primitive way to create JdbcTemplate.
You can choose the appropriate way to obtain JdbcTemplate according to your project architecture. For details, please refer to: **[Framework Integration](../../yourproject/buildtools#integration)**

Relevant Classes
- net.hasor.dbvisitor.jdbc.core.JdbcTemplate
- net.hasor.dbvisitor.jdbc.JdbcOperations

## Principle

The core implementation principle of JdbcTemplate is based on the Template pattern. Below is a core template method named `execute`:

```java title='Core Template Method'
// T is the return type of the template method
T result = jdbc.execute((ConnectionCallback<T>) con -> {
   ...
});
```

Among all template methods, `execute(ConnectionCallback)` is the most fundamental. In this basic template method, JdbcTemplate handles acquiring connections, releasing connections, and catching exceptions.
Higher-level code only needs to focus on using the Connection.

## User Guide {#guide}

- [Query](./query), execute SQL statements with return results. For example: a SELECT statement or any statement with return results.
- [Update](./update), execute SQL statements without return values. For example: INSERT, UPDATE, DELETE, or DDL operations.
- [Batch](./batch), used to execute a set of operations that can be treated as a batch.
- [Stored Procedure](./procedure), used to execute stored procedures and stored functions.
- [Rules](../../rules/about), utilize the rule mechanism to endow SQL with more powerful features.
- [Multi-value](./multi), can be used to execute SQL strings containing multiple statements and obtain all results.
- [Script](./execute), can be used to execute SQL strings containing multiple statements or load queries from external resource files.
- [Using Template](./template), operate the database via template methods.
- [Advanced Features](./feature), introducing some unique attribute functions and effects of the JdbcTemplate class.
- [Parameter Passing](../../args/about), understand different ways to pass parameters.
- [Receiving Results](../../result/about), understand different ways to receive data.
