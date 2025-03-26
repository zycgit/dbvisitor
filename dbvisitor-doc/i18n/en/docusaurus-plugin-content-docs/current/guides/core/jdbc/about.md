---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: JdbcTemplate 类
description: 了解使用 JdbcTemplate 访问数据库的准备工作和必要的概念。
---

## 准备工作

JdbcTemplate 是 dbVisitor 专门提供给基于 SQL 字符串为使用场景而设计的。API 封装了众多实用方法可以覆盖绝大多数数据库操作的需求。

- 在使用之前需要准备好数据源链接，可以是 `javax.sql.DataSource` 也可以是一个具体的 `java.sql.Connection`
- JdbcTemplate 是 **无状态** 的，可以随时创建和销毁。

下面以 `javax.sql.DataSource` 为例，使用 [HikariCP](https://github.com/brettwooldridge/HikariCP)  作为数据源。

```java title='1. 创建数据源'
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

下一步定义 JdbcTemplate，将创建好的 dbPool 数据源作为依赖参数传入 JdbcTemplate 构造方法。

```java title='2. 创建 JdbcTemplate'
JdbcTemplate jdbc = new JdbcTemplate(dbPool);
```

## 原理

JdbcTemplate 的核心实现原理是基于 Template 模式，下面是一个名为 execute 的核心模版方法：

```java title='核心模版方法'
// T 为模板方法的返回值类型
T result = jdbc.execute((ConnectionCallback<T>) con -> {
   ...
});
```

在所有模版方法中以 `execute(ConnectionCallback)` 最为基础，在这个基础模版方法中 JdbcTemplate 会处理获取连接、释放连接、捕获异常。
上层代码只需要专注于使用 Connection。

## 使用指引

- [查询](./query)，执行有返回结果的 SQL 语句。例如：一个SELECT 语句或任何带有返回结果的语句。
- [更新](./update)，执行无返回值的 SQL 语句。例如：INSERT、UPDATE、DELETE 或 DDL操作。
- [批量化](./batch)，用于执行可以作为批处理的一组操作。
- [存储过程](./procedure)，用于执行存储过程、存储函数。
- [规则](../../rules/about)，通过利用规则机制赋予 SQL 更加强大的特性。
- [多值](./multi)，可以用来执行含有多条语句的 SQL 字符串并且获取所有结果。
- [脚本](./execute)，可以用来执行含有多条语句的 SQL 字符串或者加载外部资源文件中的查询。
- [使用模版](./template)，通过模版方法来操作数据库。
- [高级特性](./feature)，介绍 JdbcTemplate 类的一些特有属性功能和效果。
- [参数传递](../../args/about)，了解使用不同的方式传进行递参数。
- [接收结果](../../result/about)，了解接收数据的不同方式。
