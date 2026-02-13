---
id: program_api
sidebar_position: 1
hide_table_of_contents: true
title: 4.1 编程式 API
description: 编程式 API 的特点是通过编程方式基于 JdbcTemplate 核心类实现对数据库的访问。
---

# 编程式 API

编程式 API 基于 [JdbcTemplate](../core/jdbc/about) 核心类，以 **原生 SQL** 为中心实现对数据库的访问。
它是 dbVisitor 中最底层、最灵活的数据访问方式，适合需要完全掌控 SQL 的场景。

:::tip[特点]
- **直接编写 SQL**，适合复杂查询、JOIN、子查询、窗口函数等场景。
- 支持 **位置参数** `?` 、**命名参数** `:name` / `#{name}` 、**PreparedStatementSetter** 三种参数传递方式。
- 支持多种 **结果集处理**：映射到 Bean、Map、单值、自定义 RowMapper。
- 内置 **批量操作** `executeBatch` 和 **存储过程调用** `call`。
- JdbcTemplate 是 **无状态** 的，可随时创建和销毁。
:::

## 创建

```java title='通过 DataSource 或 Connection 创建'
DataSource dataSource = ...
JdbcTemplate jdbc = new JdbcTemplate(dataSource);

或者

Connection conn = ...
JdbcTemplate jdbc = new JdbcTemplate(conn);
```

## 增删改

```java title='执行 DDL / DML 语句'
// DDL
jdbc.execute("create table user_info (id int primary key, name varchar(50))");

// 原始 SQL
jdbc.executeUpdate("insert into user_info (id,name) values (1, 'Bob')");

// 位置参数 (?)
jdbc.executeUpdate("insert into user_info (id,name) values (?,?)",
                   new Object[] { 2, "Alice" });

// 命名参数 (:name)
Map<String, Object> params = CollectionUtils.asMap("id", 3, "name", "David");
jdbc.executeUpdate("insert into user_info (id,name) values (:id, :name)", params);
```

## 查询

```java title='多种查询方式'
// 映射到 Bean
List<User> users = jdbc.queryForList("select * from user_info", User.class);

// 映射到 Map
List<Map<String, Object>> maps = jdbc.queryForList("select * from user_info");

// 查询单值
Long total = jdbc.queryForObject("select count(*) from user_info", Long.class);

// 自定义 RowMapper (net.hasor.dbvisitor.jdbc.RowMapper)
List<String> names = jdbc.queryForList("select * from user_info",
    (rs, rowNum) -> rs.getString("name").toUpperCase());
```

## 批量操作

```java title='批量插入、更新'
String sql = "insert into user_info (id,name) values (?,?)";
Object[][] batchArgs = {
    { 1, "Alice" },
    { 2, "Bob" },
    { 3, "Carol" }
};
int[] results = jdbc.executeBatch(sql, batchArgs);
```

## 存储过程

```java title='调用存储过程（含 INOUT 参数）'
Map<String, Object> result = jdbc.call(
    "CALL sp_add_numbers(?, ?, ?)",
    new Object[] { 10, 5, SqlArg.asInOut("result", 0, java.sql.Types.INTEGER) }
);
// result.get("result") → 15
```

:::info[有关编程式 API 的详细信息，请参阅：]
- [JdbcTemplate 类](../core/jdbc/about)
:::
